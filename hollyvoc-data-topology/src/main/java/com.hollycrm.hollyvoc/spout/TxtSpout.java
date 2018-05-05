package com.hollycrm.hollyvoc.spout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.hollycrm.kafka.consumer.DataConsumer;
import com.hollycrm.util.config.ConfigUtils;
import com.hollycrm.hollyvoc.constant.Constant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shade.storm.org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/7/6.
 *  kafka获取数据，单条获取
 */
public class TxtSpout implements IRichSpout {

    public static final String NAME = "txt-spout";
    private static final Logger logger = LoggerFactory.getLogger(TxtSpout.class);
    private DataConsumer consumer; // kafka消费者
    private SpoutOutputCollector collector;
    private Map<Integer,String> filedHBaseMap;  // 字段-顺序
    private Map<String, Integer> qcFieldMap; // 质检字段顺序
    int i =0;

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        logger.info(" spout open..........");
        this.collector = spoutOutputCollector;
        Properties props = ConfigUtils.getProp("/consumer.properties");
        Long interval = ConfigUtils.getLongVal("poll.interval.mills", 2000L);
//        String group = (String)conf.get(MSG_GROUP);
        String group = "basic-txt-topology";
        // 初始化cusumer
        this.consumer = new DataConsumer(BASIC_TOPIC, group == null ? TXT_MSG_GROUP_TOPOLOGY : group,
                props, interval);
        Map<String,Integer>  map = Constant.getHBaseMapping();
        filedHBaseMap = new HashMap<>(map.size());
        map.forEach((k, v) -> {
            filedHBaseMap.put(v, k);
        });

        qcFieldMap = Constant.getQcMatchMapping();

    }

    @Override
    public void close() {
        consumer.close();
    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void nextTuple() {

        this.consumer.pollAndProcessMsg(crs -> {
            Iterator<ConsumerRecord<String, String>> iterator = crs.iterator();
            logger.info(" data size :" + crs.count());
            if(crs.count()==0){
                logger.info("emit count : " + i);
            }
            while (iterator.hasNext()){
                // 对每条数据进行操作
                Long start = System.currentTimeMillis();
                ConsumerRecord<String, String> record = iterator.next();
                String rowKey;
                String key = record.key();
                String value = record.value();
//                logger.info("value:" + value);
//                logger.info("record offset :" +record.offset());
                String[] keys = key.split(DELIMITER_FIELDS); // 对key进行处理，如果是普通数据，key=rowkey，异常数据key=rowkey#标识符
                String mark = ""; // 数据标识
                String streamId = TOPOLOGY_STREAM_TXT_ID; // 默认是正常数据
                Values values = new Values(); // 默认的处理方式
                if(keys.length==2){
                   rowKey = keys[0];
                    mark = keys[1];
                } else {
                    // 正常数据
                    rowKey = key;
                    values = txtPretreat(rowKey, value, filedHBaseMap);
                }

                try {
//                    System.out.println("rowKey" + rowKey + " prov : " + rowKey.substring(10, 12));
//                    logger.info("rowKey" + rowKey + " prov : " + rowKey.substring(10, 12));

                    switch (mark) {
                        case HBASE_MARK:
                            streamId = TOPOLOGY_STREAM_TXT_ID;
                            values = txtPretreat(rowKey, value, filedHBaseMap);
                            break;
                        case INDEX_MARK:
                            streamId = TOPOLOGY_STREAM_HBASE_ID;
                            values = txtPretreat(rowKey, value, filedHBaseMap);
                            break;
//                        case QUALITY_MARK:
//                            streamId = TOPOLOGY_STREAM_QC_ID;
//                            // 质检异常数据与默认的处理方式不同
//                            values = qcErrPretreat(rowKey, value, qcFieldMap);
//                            break;
                        case  NW_MARK:
                            // todo 发现新词异常处理
                            streamId = TOPOLOGY_STREAM_NW_ERR_ID;
                            values = new Values(value);
                            break;
                        case NW_REDIS_MARK:
                            // todo 新词发送给redis异常
                            streamId = TOPOLOGY_STREAM_NW2REDIS_ERR_ID;
                            values = new Values(rowKey,value);
                            break;

                    }

                    // 有数据才会发送给下游，否则不发送数据
                    if(values.size()>0){
                        // 发送数据到下游
                        emitData(streamId, values, collector);
                        i++;
                        logger.info("emit index :" + i);
                    }

                } catch (Exception e) {
                    // 属于代码异常
                    logger.error("spout has error", e);
                }
//                logger.info(" finish one data , rowKey: " + rowKey);
            }

            return true;
        });
    }

    private void emitData(String streamId, Values values, SpoutOutputCollector collector) {
        collector.emit(streamId,values);
    }

    @Override
    public void ack(Object o) {

    }

    @Override
    public void fail(Object o) {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream(
                TOPOLOGY_STREAM_TXT_ID,
                new Fields(DEC_PROVINCE, DEC_ROW_KEY, DEC_DAY,
                        DEC_BASIC_INFO, DEC_USER_TXT, DEC_AGENT_TXT, DEC_ALL_TXT)
        );
        outputFieldsDeclarer.declareStream(
                TOPOLOGY_STREAM_HBASE_ID,
                new Fields(DEC_PROVINCE, DEC_ROW_KEY,  DEC_DAY,
                        DEC_BASIC_INFO, DEC_USER_TXT, DEC_AGENT_TXT, DEC_ALL_TXT)
        );
        // 发送到oracle-bolt
        outputFieldsDeclarer.declareStream(TOPOLOGY_STREAM_QC_ID, new Fields(
                qcId, qcName, id, qcType, qcTxtContent, qcMatchWord,
                province, custBrand, satisfication, businessType,
                silenceLength, userCode,acceptTime,caller,callee));

        // 发送给newWordBolt
        outputFieldsDeclarer.declareStream(TOPOLOGY_STREAM_NW_ERR_ID, new Fields(DEC_ALL_TXT));
        // 发送给nw2redisbolt
        outputFieldsDeclarer.declareStream(TOPOLOGY_STREAM_NW2REDIS_ERR_ID, new Fields(DEC_WORD, DEC_FREQ));

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    /**
     * 处理正常数据、hbase异常数据、index异常数据。
     * @param value 文本信息
     * @param rowKey rowKey
     */
    private static Values txtPretreat(String rowKey, String value, Map<Integer,String> filedHBaseMap){

        String[] infos = value.split("\\" + DELIMITER_PIPE); // “|”切分数据是需要转译
        String userTxt="",agentTxt="",allTxt="";
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<infos.length;i++){
            // 找到文本的值添
            String val = infos[i];
            String file = filedHBaseMap.get(i);
            if(userContent.equals(file)){
                userTxt = val;
                continue;
            }
            if(agentContent.equals(file)){
                agentTxt = val;
                continue;
            }
            if(allContent.equals(file)){
                allTxt = val;
                continue;
            }
            builder.append(val).append(DELIMITER_PIPE); // 个字段之间用“|”拼接
        }
        String basicInfo = builder.substring(0,builder.length()-1);

//        System.out.println("rowKey" + rowKey + " prov : " + rowKey.substring(10, 12));
        // 发送到下游的数据,如果rowkey格式有问题，省份截取不到，在下游的index就会出错
        return new Values(
                rowKey.substring(10, 12), // 省份
                rowKey, // rowkey
                StringUtils.reverse(rowKey.substring(0, 10))
                        .substring(0, 8), // 日期
                basicInfo, // info:contact
                userTxt, // 客户文本
                agentTxt, // 坐席
                allTxt // 全部通话内容
        );

    }

    /**
     * 针对质检保存到数据库异常的数据.
     * @param rowKey rowkey
     * @param value 异常数据信息
     * @param fileds  字段-顺序集合
     * @return 返回要发送到下游的数据
     */
    private static Values qcErrPretreat(String rowKey, String value, Map<String, Integer> fileds){
        String[] qc = value.split(DELIMITER_FIELDS); // “#”切分数据是需要转译
//        System.out.println("qc " + qc.length +" " + StringUtils.join(qc, "-"));
        return new Values(
                qc[fileds.get(qcId)], qc[fileds.get(qcName)], qc[fileds.get(Constant.rowkey)],
                qc[fileds.get(qcType)], qc[fileds.get(qcTxtContent)], qc[fileds.get(qcMatchWord)],
                qc[fileds.get(province)], qc[fileds.get(custBrand)], qc[fileds.get(satisfication)],
                qc[fileds.get(businessType)], qc[fileds.get(silenceLength)], qc[fileds.get(userCode)],
                qc[fileds.get(acceptTime)], qc[fileds.get(caller)], qc[fileds.get(callee)]);
    }
}
