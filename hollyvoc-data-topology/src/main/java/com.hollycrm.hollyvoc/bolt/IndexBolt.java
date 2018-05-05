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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/8/16.
 * 批量索引
 * 需要将省份分开，按照省份上传。
 * map<String,List<SolrInputDocument>> 省份和需要索引的字段。
 * 考虑同时传来的可能是多各省份的数据，如果某个省份不足1000的情况，定义时间戳
 */
public class IndexBolt implements IRichBolt {
    private static Logger logger = LoggerFactory.getLogger(IndexBolt.class);

    public final static String NAME = "index-bolt";
    private Map<String, Integer> mapping; // 需要索引的字段
    private SolrClient client;
    private Integer sheetNoIdx;
    private ConcurrentHashMap<String,List<SolrInputDocument>> resource; // 存放各省份需要进行索引的字段。
    private ConcurrentHashMap<String,List<Values>> emitData; // 需要发送给下游的数据，即提交异常的数据
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ConcurrentHashMap<String,Long> timeMap;
    private OutputCollector collector;
    private AtomicInteger counter = new AtomicInteger(0);
    private AtomicInteger emitcounter = new AtomicInteger(0);
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector collector) {
        this.collector = collector;
        resource = new ConcurrentHashMap<>();
        emitData = new ConcurrentHashMap<>();
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
            if (TOPOLOGY_STREAM_HBASE_ID.equals(input.getSourceStreamId())) {
                String t = Thread.currentThread().getName();
                counter.incrementAndGet();

                String rowKey = input.getStringByField(TopoConstant.DEC_ROW_KEY);

                String basicInfo = input.getStringByField(TopoConstant.DEC_BASIC_INFO);
                String agentTxt = input.getStringByField(TopoConstant.DEC_AGENT_TXT);
                String userTxt = input.getStringByField(DEC_USER_TXT);
                String allTxt = input.getStringByField(DEC_ALL_TXT);
                String prov = input.getStringByField(TopoConstant.DEC_PROVINCE);
                List<Values> tempValues = new ArrayList<>();
                try {
                    String[] bis = basicInfo.split("\\" + Constant.DELIMITER_PIPE);
                    SolrInputDocument doc = transferDoc(rowKey, bis, userTxt, agentTxt, allTxt);

                    resource.computeIfAbsent(prov, list -> new ArrayList<>(SOLR_BATCHSIZE));
                    List<SolrInputDocument> docs = resource.get(prov);
                    docs.add(doc);
                    emitData.computeIfAbsent(prov, list -> new ArrayList<>(SOLR_BATCHSIZE));
                    List<Values> values = emitData.get(prov);
                    values.add(new Values(rowKey, prov, basicInfo, agentTxt, userTxt, allTxt));
                    try {
                        if (docs.size() == SOLR_BATCHSIZE) { // 满足1000条
                            // 提交hbase
                            List<SolrInputDocument> temp = new ArrayList<>(docs.size()); // 备份索引数据
                            temp.addAll(docs);
                            docs.clear(); // 清空数据
                            // 提交数据到hbase，并发送数据给下游，提交失败时会发送给err-bolt
                            tempValues.addAll(values);
                            commit(prov, temp);
                            temp.clear(); // 提交成功后清除数据
                            values.clear();
                        }
                    } catch (SolrServerException e) {
//                    System.out.println("solr has SolrServerException " + e);
                        logger.error("solr has error", e);

                        // 异常将消息发送给kafka index-error
                        emitData(tempValues, collector, emitcounter);
                        tempValues.clear();
//                throw new FailedException("消息处理失败");
                    } catch (IOException e) {
                        logger.error("solr has error", e);
                        initSolrClient();
                        //  异常将消息发送给kafka index-error
                        emitData(tempValues, collector, emitcounter);
                        tempValues.clear();
                        // 显示的抛出FailedException异常(jStorm发现这个错误之后会自动处理)
                        //                throw new FailedException("消息处理失败");
                    }
                } catch (Exception e) {
                    logger.error("index has error", e);
                }
                timeMap.put(prov, System.currentTimeMillis());

            } else {
                // 如果超过两秒没有数据流入，不满足条件的也需要提交
                timeMap.forEach((k, v) -> {
                    if (((System.currentTimeMillis() - v) / 1000) > TIME_OUT) { // 超过2秒没有接受到数据，需要提交。
                        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
                        List<SolrInputDocument> data = resource.get(k);
                        List<Values> values = emitData.get(k);
                        boolean isLock = writeLock.tryLock();
                        logger.info(" index " + k + " accept-data: " + counter + " emit-data:" + emitcounter);

                        // 获取锁并且有数据在可以提交
                        while (isLock) {
                            // 有数据才提交
                            if (data.size() > 0) {
                                List<SolrInputDocument> tempDoc = new ArrayList<>();
                                tempDoc.addAll(data);
                                data.clear();
                                List<Values> tempEmit = new ArrayList<>();
                                tempEmit.addAll(values);
                                values.clear();
                                writeLock.unlock();
                                logger.info(" index" + k + "lock-- data szie:" + tempDoc.size());
                                try {
                                    commit(k, tempDoc);
                                } catch (Exception e) {
                                    // 发送error数据给kafka
                                    emitData(tempEmit, this.collector, emitcounter);
                                    tempEmit.clear();
                                }
//                            logger.info(k + " clean ls1 " + tempDoc.size());
                                tempDoc.clear();
                            } else {
                                writeLock.unlock();
                            }
                            isLock = false;
                        }
                        // 更新时间
                        timeMap.put(k, System.currentTimeMillis());
                    }

                });
            }
            collector.ack(input);
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
    private void commit(String prov, List<SolrInputDocument> docs) throws SolrServerException, IOException{
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
        System.out.println("error size " + values.size());
    }
}
