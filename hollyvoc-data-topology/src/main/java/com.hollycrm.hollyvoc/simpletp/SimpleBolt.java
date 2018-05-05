package com.hollycrm.hollyvoc.simpletp;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by qianxm on 2017/12/26.
 */
public class SimpleBolt implements IBasicBolt {
    public final static String NAME = "simple-bolt";

    private static Logger logger = LoggerFactory.getLogger(SimpleBolt.class);
    @Override
    public void prepare(Map stormConf, TopologyContext context) {

    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        if("stream-id".equals(input.getSourceStreamId())) {
            try {
                Long value = input.getLongByField("value");
                // todo 对value 逻辑处理
                logger.info(" 接受到的值是： " + value);
            } catch (Exception e) {

            }
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
