package com.hollycrm.hollyvoc.qc.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.hollycrm.kafka.producer.DataProducer;
import com.hollycrm.util.config.ConfigUtils;
import com.hollycrm.hollyvoc.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static com.hollycrm.hollyvoc.qc.QCConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.DELIMITER_PIPE;
import static com.hollycrm.hollyvoc.constant.Constant.ERR_TOPIC;

/**
 * Created by qianxm on 2017/8/14.
 * 发送异常数据到kafka. hbase保存异常、index异常、新词发现
 */
public class ErrProducerBolt implements IRichBolt {
    public static final String NAME = "error-bolt";
    private static Logger logger = LoggerFactory.getLogger(ErrProducerBolt.class);
    private DataProducer producer;
    private OutputCollector collector;
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        Properties prop = ConfigUtils.getProp("/producer.properties");
        // 初始化producer
        this.producer = new DataProducer(ERR_TOPIC, prop);
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        try{
            switch (input.getSourceStreamId()) {
                // todo 对质检和保存到数据库中的数据异常数据，发送到kafka
                case TOPOLOGY_STREAM_QC_ERR_ID:
                    // 质检异常
                    sendToKafka(input, QUALITY_MARK, producer);
                    break;
                case TOPOLOGY_ORACLE_ERR_ID:
                    // 保存数据异常
                    // todo 保存格式不同
//                    sendToKafka(input, INDEX_MARK, producer);
                    break;
            }

        } catch (Exception e) {
            logger.error(" send err data to  kafka error! ", e);
        }
        collector.ack(input);
    }


    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    private static void sendToKafka(Tuple input, String errMark, DataProducer producer){
        String rowKey = input.getStringByField(DEC_ROW_KEY);
        System.out.println("rowkey" + rowKey);
        String basicInfo = input.getStringByField(DEC_BASIC_INFO);
        String agentTxt = input.getStringByField(DEC_AGENT_TXT);
        String userTxt = input.getStringByField(DEC_USER_TXT);
        String allTxt = input.getStringByField(DEC_ALL_TXT);
        StringBuilder values = new StringBuilder();
        // todo 不同的标志value不同
        values.append(basicInfo).append(DELIMITER_PIPE)
                .append(agentTxt).append(DELIMITER_PIPE)
                .append(userTxt).append(DELIMITER_PIPE)
                .append(allTxt);
        System.out.println(values.toString());
        try {
            // key格式 rowkey#h
            System.out.println(" send to kafka key" + rowKey+ Constant.DELIMITER_FIELDS + errMark);
            System.out.println(" value " + values.toString());
            producer.sendMsg(rowKey+ Constant.DELIMITER_FIELDS + errMark, values.toString());
        }catch (Exception e) {
            logger.error("send  data to kafka has error " + rowKey, e);
        }
    }


}
