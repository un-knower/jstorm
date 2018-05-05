package com.hollycrm.hollyvoc.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.hollyvoc.helper.redis.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;

/**
 * Created by qianxm on 2017/8/17.
 * 将新词发送到kafka
 */
public class NW2RedisBolt implements IRichBolt {

    public final static String NAME = "nw2Redis-bolt";

    private static Logger logger = LoggerFactory.getLogger(NW2RedisBolt.class);

    private OutputCollector collector;
    private int i=0;
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        try {
            // 接受的到新词发送给redis
            if (TOPOLOGY_STREAM_NW_ID.equals(input.getSourceStreamId()) || TOPOLOGY_STREAM_NW2REDIS_ERR_ID.equals(input.getSourceStreamId())) {
                String word = input.getStringByField(DEC_WORD);
                String freq = input.getStringByField(DEC_FREQ);
                try {
                    logger.debug(" new word to redis ");
                    RedisHelper.getInstance().add2Hash(REDIS_NW_KEY, word, freq);
                    i++;
                    logger.debug("send to redis: " + i);
                } catch (Exception e) {
                    logger.error(" nw2redis error !", e);
                    i++;
                    logger.debug("send to kafka: " + i);
                    // 发送失败，将异常消息发送给kafka
                    collector.emit(TOPOLOGY_STREAM_NW2REDIS_ERR_ID, new Values(word, freq));
                }
                logger.debug(" commit to redis finish! ");
            }
            collector.ack(input);
        } catch (Exception e) {
            collector.fail(input);
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // 发送给redis异常
        declarer.declareStream(TOPOLOGY_STREAM_NW2REDIS_ERR_ID, new Fields(DEC_WORD, DEC_FREQ));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
