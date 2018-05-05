package com.hollycrm.hollyvoc.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.hollycrm.hollyvoc.constant.TopoConstant;
import com.hollycrm.kafka.producer.DataProducer;
import com.hollycrm.util.config.ConfigUtils;
import com.hollycrm.hollyvoc.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.hollyvoc.constant.ConstUtils.javaId;
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
                case TOPOLOGY_STREAM_HBASE_COMMIT_ERR_ID:
                    // hbase error 提交hbase异常
                    sendToKafka(input, HBASE_MARK, producer);
                    break;
                case TOPOLOGY_STREAM_INDEX_ERR_ID:
                    // index error 索引
                    sendToKafka(input, INDEX_MARK, producer);
                    break;
                case  TOPOLOGY_STREAM_NW_ERR_ID:
                    // 学习新词异常
//                    logger.info(" new word error! ");
                    nwError(input, NW_MARK, producer);
                    break;
                case TOPOLOGY_STREAM_NW2REDIS_ERR_ID:
                    // new word to redis error 新词保存到redis异常
                    nw2RedisError(input, NW_REDIS_MARK, producer);
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
        String allTxt = input.getStringByField(TopoConstant.DEC_ALL_TXT);
        StringBuilder values = new StringBuilder();
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

    /**
     * 新词存储到redis异常，将数据发送到kafka error-topic. key-word,value-freq.
     * @param input 新词数据
     * @param errMark 错误标志
     * @param producer kafka producer
     */
    private static void nw2RedisError(Tuple input, String errMark, DataProducer producer){
        // todo 处理新词发送个到redis的错误，key是词#r  ,value 是词性
        // TODO 验证kafka的key值是否可以重复，如果重复读取到的数据是否是双份
        try{
            String word = input.getStringByField(DEC_WORD);
            String freq = input.getStringByField(DEC_FREQ);
            producer.sendMsg(word + Constant.DELIMITER_FIELDS + errMark, freq);
        } catch (Exception e) {
            logger.error(" send nw2RedisError data to kafka has error! ", e);
        }
    }

    /**
     * 学习新词异常，将数据发送到kafka error-topic. key-随机生成的唯一key值,value-contents 多条文本.
     * @param input 新词数据
     * @param errMark 错误标志
     * @param producer kafka producer
     */
    private static void nwError(Tuple input, String errMark, DataProducer producer) {
        try{
//            String rowkey = input.getStringByField(DEC_ROW_KEY);
            String contents = input.getStringByField(DEC_ALL_TXT);

            producer.sendMsg(javaId() + Constant.DELIMITER_FIELDS + errMark, contents);
            logger.info("send to kafka key:", javaId() + Constant.DELIMITER_FIELDS + errMark);
            System.out.println("send to kafka key:"+ javaId() + Constant.DELIMITER_FIELDS + errMark);
        } catch (Exception e) {
            logger.error(" send nwError data to kafka has error! ", e);
        }
    }
}
