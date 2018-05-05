package com.hollycrm.hollyvoc.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.hollycrm.hollyvoc.constant.TopoConstant;
import com.hollycrm.kafka.producer.DataProducer;
import com.hollycrm.util.config.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.DELIMITER_PIPE;
import static com.hollycrm.hollyvoc.constant.Constant.QC_TOPIC;

/**
 * Created by qianxm on 2017/8/18.
 * 将hbase保存成功的数据发送给kafka,用来质检.
 * topic name : qc-topic
 */
public class HbaseProducerBolt implements IRichBolt{

    public static final String NAME = "hbase2kafka-bolt";
    private static Logger logger = LoggerFactory.getLogger(HbaseProducerBolt.class);
    private DataProducer producer;
    private OutputCollector collector;
    private AtomicInteger counter = new AtomicInteger(0);


    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        Properties prop = ConfigUtils.getProp("/producer.properties");
        // 初始化producer
        this.producer = new DataProducer(QC_TOPIC, prop);
        this.collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
        try {
            if (TOPOLOGY_STREAM_HBASE_ID.equals(tuple.getSourceStreamId())) {
                String rowKey = tuple.getStringByField(TopoConstant.DEC_ROW_KEY);
                counter.incrementAndGet();
                String basicInfo = tuple.getStringByField(TopoConstant.DEC_BASIC_INFO);
                String agentTxt = tuple.getStringByField(TopoConstant.DEC_AGENT_TXT);
                String userTxt = tuple.getStringByField(DEC_USER_TXT);
                String allTxt = tuple.getStringByField(DEC_ALL_TXT);
                StringBuilder values = new StringBuilder();
                values.append(basicInfo).append(DELIMITER_PIPE)
                        .append(userTxt).append(DELIMITER_PIPE)
                        .append(agentTxt).append(DELIMITER_PIPE)
                        .append(allTxt);
//                System.out.println(values.toString());
                try {
                    // key格式 rowkey#h
//                System.out.println(" send to kafka key" + rowKey);
//                System.out.println(" value " + values.toString());
                    producer.sendMsg(rowKey, values.toString());
                } catch (Exception e) {
                    logger.error("send  data to kafka has error " + rowKey, e);
                }
            } else {
                logger.info(" accept " + counter);
            }
            collector.ack(tuple);
        } catch (Exception e) {
            collector.fail(tuple);
        }
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
}
