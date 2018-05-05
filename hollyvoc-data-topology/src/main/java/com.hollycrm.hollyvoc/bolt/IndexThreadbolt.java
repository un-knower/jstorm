package com.hollycrm.hollyvoc.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.hollycrm.hollyvoc.constant.TopoConstant;
import com.hollycrm.util.config.ConfigUtils;
import com.hollycrm.hollyvoc.constant.ConstUtils;
import com.hollycrm.hollyvoc.constant.Constant;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/9/8.
 * 资源隔离做法
 * 思路： 根据线程名称隔离每个线程中的数据，每个线程中，根据省份区分不同省份的数据并做批量提交。
 */
public class IndexThreadbolt implements IRichBolt{
    private static Logger logger = LoggerFactory.getLogger(IndexNewBolt.class);

    public final static String NAME = "index-thread-bolt";
    private Map<String, Integer> mapping; // 需要索引的字段
    private SolrClient client;
    private Integer sheetNoIdx;
//    private ConcurrentHashMap<String,List<SolrInputDocument>> resource; // 存放各省份需要进行索引的字段。
//    private ConcurrentHashMap<String,List<Values>> emitData; // 需要发送给下游的数据，即提交异常的数据, 省份-list
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ConcurrentHashMap<String,Long> timeMap; // 线程-时间
    private OutputCollector collector;
    private AtomicInteger counter = new AtomicInteger(0);
    private AtomicInteger emitcounter = new AtomicInteger(0); // 线程安全
    private ConcurrentHashMap<String,Integer> threadCounter; // 数据总量和task

    private ConcurrentHashMap <String, Map<String,List<SolrInputDocument>>>  threadData; // 线程中各省份数据
    private ConcurrentHashMap <String, Map<String,List<Values>>> threadValues; // 线程中各省份接受的数据

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
//        resource = new ConcurrentHashMap<>();
        threadCounter = new ConcurrentHashMap<>();
        threadData = new ConcurrentHashMap<>();
        threadValues = new ConcurrentHashMap<>();
//        emitData = new ConcurrentHashMap<>();
        timeMap = new ConcurrentHashMap<>();
        mapping = Constant.getHBaseMapping();
        mapping.remove(recordName);
        this.sheetNoIdx = mapping.get(sheetNo);
        mapping.remove(sheetNo);
        mapping.remove(recordFormat);
        mapping.remove(recordEncodeRate);
        mapping.remove(recordSampRate);
        mapping.remove(custinfoId);
        mapping.remove(userContent);
        mapping.remove(agentContent);
        mapping.remove(allContent);
        initSolrClient(); // 初始化solr
    }

    /**
     * 初始化solrClient
     */
    private void initSolrClient() {
        CloudSolrClient cloudSolrClient = new CloudSolrClient.Builder()
                .withZkHost(ConfigUtils.getStrVal("solr.zk"))
                .build();
        cloudSolrClient.setZkConnectTimeout(100000); // zk的超时时间
        this.client = cloudSolrClient;
    }

    @Override
    public void execute(Tuple input) {
        try {
            String threadName = Thread.currentThread().getName();
            if (TOPOLOGY_STREAM_HBASE_ID.equals(input.getSourceStreamId())) {

                counter.incrementAndGet();
                // 应该是没有数据流入超过2秒再提交
                String rowKey = input.getStringByField(TopoConstant.DEC_ROW_KEY);

                String basicInfo = input.getStringByField(TopoConstant.DEC_BASIC_INFO);
                String agentTxt = input.getStringByField(TopoConstant.DEC_AGENT_TXT);
                String userTxt = input.getStringByField(DEC_USER_TXT);
                String allTxt = input.getStringByField(DEC_ALL_TXT);
                String prov = input.getStringByField(TopoConstant.DEC_PROVINCE);
                // 每个线程接入数据的时间
                timeMap.put(threadName, System.currentTimeMillis());
//                System.out.println(" index rowkey " + rowKey + " prov " +prov);
//                List<Values> tempValues = new ArrayList<>(); // 发送到下游数据的备份
                // 每个线程接受到的数量总数
                threadCounter.computeIfAbsent(threadName,i-> 0);
                int count = threadCounter.get(threadName);
                count++;
                threadCounter.put(threadName, count);
//                System.out.println(" count:" + count + " emitcounter: " + counter);
                logger.info(" count:" + count + " counter: " + counter + " accpet " +counter );
                try {
                    String[] bis = basicInfo.split("\\" + Constant.DELIMITER_PIPE);
                    SolrInputDocument doc = transferDoc(rowKey, bis, userTxt, agentTxt, allTxt);
                    threadData.computeIfAbsent(threadName,map->new HashMap<>(30));
                    Map<String, List<SolrInputDocument>> proDatas = threadData.get(threadName);
                    proDatas.computeIfAbsent(prov, list -> new ArrayList<>(SOLR_BATCHSIZE));
                    List<SolrInputDocument> docs = proDatas.get(prov);
                    docs.add(doc);
                    proDatas.put(prov, docs);
                    // 接受到的数据，如果出现异常发送给kafka
                    threadValues.computeIfAbsent(threadName, map -> new HashMap<>());
                    // 每个线程中的数据是分省份的
                    Map<String, List<Values>> emitData = threadValues.get(threadName);
                    emitData.computeIfAbsent(prov, list -> new ArrayList<>(SOLR_BATCHSIZE));
                    List<Values> values = emitData.get(prov);
                    values.add(new Values(rowKey, prov, basicInfo, agentTxt, userTxt, allTxt));
                    emitData.put(prov,values);


                    if (count == SOLR_BATCHSIZE) { // 满足1000条
                        // 1. 备份数据

                        Map<String, List<SolrInputDocument>> tempDatas  = new HashMap<>(30);
                        Map<String, List<Values>> tempEmit = new HashMap<>();
                        tempDatas.putAll(proDatas);
                        threadData.remove(threadName);  // 清空当前线程中的数据
                        tempEmit.putAll(emitData);
                        threadValues.remove(threadName); // 清空当前线程中的数据

                        logger.info("backup clear proDatas: " + tempDatas.size() + " emitData: " + tempEmit.size());

                        // 2. 循环map，提交各省份数据
                        threadCounter.put(threadName, 0); // 清空线程接受数据的数量

                        // 每个省份有数据  如果同时有30个省份的数据提交会耗费时间
                        tempDatas.forEach((k,v)->{
//                            System.out.println(" v "+ k +"datalist: " + v.size());
                            logger.info(" prov: "+ k +"datalist: " + v.size());

                            if(v.size() > 0) { // 每个省份有数据
//                                System.out.println(" prov:"+ k + " data: " + v.size());
                                // 提交solr
                                try {
                                    commit(k, v);
                                    logger.info("commit : " + v.size() + " prov: " + k);

                                } catch (SolrServerException e) {
                                    logger.error("solr has error", e);
                                    // 异常将消息发送给kafka index-error
                                    emitData(tempEmit.get(k), collector, emitcounter);

                                } catch (IOException e) {
                                    logger.error("solr has error", e);
                                    initSolrClient();
                                    //  异常将消息发送给kafka index-error
                                    emitData(tempEmit.get(k), collector, emitcounter);
                                    // 显示的抛出FailedException异常(jStorm发现这个错误之后会自动处理)
                                    //                throw new FailedException("消息处理失败");
                                }
                                timeMap.put(prov, System.currentTimeMillis());
                            }
                        });

                    }
                } catch (Exception e) {
                    logger.error("index has error", e);
                }
                collector.ack(input);
            } else {
                Long timeOut = timeMap.get(threadName);
                // 只操作本线程中的数据即可
                if(timeOut != null && (System.currentTimeMillis() - timeOut) / 1000 > TIME_OUT) {
                    timeMap.remove(threadName);
                    threadCounter.put(threadName, 0); // 清空线程接受数据的数量
                    // 如果超时未接受到数据，提交缓存数据
                    Map<String,List<SolrInputDocument>>  backupData = new HashMap<>();
                    Map<String,List<Values>> backupValue = new HashMap<>();
                    backupData.putAll(threadData.get(threadName));
                    backupValue.putAll(threadValues.get(threadName));
                    threadData.remove(threadName);
                    threadValues.remove(threadName);
                    logger.info(" timeout data: " + backupData.size() + " value: " + backupValue.size() + " emit: " + emitcounter);
                    // 把各省份缓存的数据提交
                    backupData.forEach((k,v)->{
                        if(v.size() > 0){
                            // 有数据才进行提交
                            try {
                                logger.info(" timeout cimmit! ");
                                commit(k, v);
                            } catch (Exception e) {
                                // 发送error数据给kafka
                                emitData(backupValue.get(k), this.collector, emitcounter);
                            }
                        }
                    });
                }

            }

        } catch (Exception e) {
            collector.fail(input);
        }

    }

    /**
     * 创建索引
     * @param prov 省份
     * @param docs 索引文档
     * @throws SolrServerException solr异常
     * @throws IOException 异常
     */
    private void commit(String prov, List<SolrInputDocument> docs) throws SolrServerException, IOException {
        // todo solr collection 名称需要根据年份和省份确定
        this.client.add(ConfigUtils.getStrVal("solr.prefix") + prov, docs,
                ConfigUtils.getIntVal("solr.waitCommitMills", 5000));
        logger.info("========== creater index finsh! docs.size = " +  docs.size() + " prov: " + prov);
        System.out.println("============== creater index finsh! docs.size = " +  docs.size() + " prov: " + prov);
    }

    private SolrInputDocument transferDoc(String rowKey, String[] bis, String userTxt, String agentTxt, String allTxt) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField(id, rowKey);
        doc.setField(allContent, allTxt);
        doc.setField(agentContent, agentTxt);
        doc.setField(userContent, userTxt);
//        doc.getFieldValue(id);

        String sheet ;
        if(sheetNoIdx == null){
            sheet = null;
        }else {
            sheet = bis[sheetNoIdx];
        }
        // 判断是否有工单
        doc.setField(hasSheet, sheet == null || "".equals(sheet) || "null".equals(sheet) ? "0" : "1");

        long duration =  Long.parseLong(bis[mapping.get(recordLength)]);
        long silence = Long.parseLong(bis[mapping.get(silenceLength)]);
        doc.setField(recordLengthRange, ConstUtils.getRecoinfoLengthRangeCode(duration));
        doc.setField(silenceLengthRange, ConstUtils.getSilenceRangeCode(duration, silence));

        mapping.forEach((field, idx) -> doc.setField(field, bis[idx]));

        return doc;
    }


    @Override
    public void cleanup() {
        try{
            this.client.close();

        }catch (Exception e) {
            logger.error(" close solrClient error  ： " , e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(TOPOLOGY_STREAM_INDEX_ERR_ID,new Fields(
                DEC_ROW_KEY,DEC_PROVINCE, DEC_BASIC_INFO, DEC_AGENT_TXT, DEC_USER_TXT, DEC_ALL_TXT));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    /**
     * 发送异常数据.
     * @param basicOutputCollector collector
     */
    private void emitData(List<Values> values, OutputCollector basicOutputCollector, AtomicInteger emitcounter) {
        values.forEach( value -> {
            basicOutputCollector.emit(TOPOLOGY_STREAM_INDEX_ERR_ID, value);
            emitcounter.incrementAndGet();
        });
        logger.info("error size " + values.size() + " prov: " +values.get(1));
//        System.out.println("error size " + values.size());
    }
}
