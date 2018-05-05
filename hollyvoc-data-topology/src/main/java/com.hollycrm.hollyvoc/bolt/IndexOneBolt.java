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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/9/8.
 */
public class IndexOneBolt implements IRichBolt{
    private static Logger logger = LoggerFactory.getLogger(IndexOneBolt.class);
    public final static String NAME = "index-one-bolt";

    private Map<String, Integer> mapping; // 需要索引的字段
    private SolrClient client;
    private Integer sheetNoIdx;
    private OutputCollector collector;
    private AtomicInteger emitcounter = new AtomicInteger(0); // 线程安全

    /**
     * 初始化solrClient
     */
    private void initSolrClient() {
        CloudSolrClient cloudSolrClient = new CloudSolrClient.Builder()
                .withZkHost(ConfigUtils.getStrVal("solr.zk"))
                .build();
        cloudSolrClient.setZkConnectTimeout(100000); // 这只zk的超时时间
        this.client = cloudSolrClient;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        try{
            System.out.println("IndexBolt -- prepare");
            logger.info("IndexBolt -- prepare");
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
            this.collector = collector;
            initSolrClient(); // 初始化solr
        } catch (Exception e) {
            throw new RuntimeException(" init solrClient error! ", e);
        }
    }

    @Override
    public void execute(Tuple input) {
        try{
            if(TOPOLOGY_STREAM_HBASE_ID.equals(input.getSourceStreamId())){
                System.out.println("index-execute: "+Thread.currentThread().getName());
                logger.info(" accept "+ emitcounter.incrementAndGet());
//                logger.info("index-execute: "+Thread.currentThread().getName());
                String rowKey = input.getStringByField(TopoConstant.DEC_ROW_KEY);
                String basicInfo = input.getStringByField(TopoConstant.DEC_BASIC_INFO);
                String agentTxt = input.getStringByField(TopoConstant.DEC_AGENT_TXT);
                String userTxt = input.getStringByField(DEC_USER_TXT);
                String allTxt = input.getStringByField(DEC_ALL_TXT);
                String prov = input.getStringByField(TopoConstant.DEC_PROVINCE);
                try {

                    logger.info(" prov : " + prov);
                    String[] bis = basicInfo.split("\\"+Constant.DELIMITER_PIPE);
                    SolrInputDocument doc = transferDoc(rowKey, bis, userTxt, agentTxt, allTxt);
                    commit(prov, doc);
                }catch (SolrServerException e) {
                    e.printStackTrace();
                    logger.error("solr has error", e);
                    // todo 异常将消息发送给kafka index-error
                    collector.emit(TOPOLOGY_STREAM_INDEX_ERR_ID,new Values(
                            rowKey, prov, basicInfo, agentTxt, userTxt, allTxt));
                }catch (IOException e) {
                    logger.error("solr has error", e);
                    e.printStackTrace();
                    initSolrClient();
                    // todo 异常将消息发送给kafka index-error
                    collector.emit(TOPOLOGY_STREAM_INDEX_ERR_ID,new Values(
                            rowKey, prov, basicInfo, agentTxt, userTxt, allTxt));
                }
            }
            collector.ack(input);
        } catch (Exception e) {
            logger.error(" index error!");
            collector.fail(input);
        }
    }

    /**
     * 创建索引
     * @param prov 省份
     * @param doc 索引文档
     * @throws SolrServerException solr异常
     * @throws IOException 熊异常
     */
    private void commit(String prov, SolrInputDocument doc) throws SolrServerException, IOException{
        this.client.add(ConfigUtils.getStrVal("solr.prefix") + prov, doc,
                ConfigUtils.getIntVal("solr.waitCommitMills", 5000));
        logger.info(" creater index finsh! ");
    }

    private SolrInputDocument transferDoc(String rowKey, String[] bis, String userTxt, String agentTxt, String allTxt) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField(id, rowKey);
        doc.setField(allContent, allTxt);
        doc.setField(agentContent, agentTxt);
        doc.setField(userContent, userTxt);

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
}
