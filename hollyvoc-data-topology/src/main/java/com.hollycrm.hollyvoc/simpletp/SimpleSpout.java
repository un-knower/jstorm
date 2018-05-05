package com.hollycrm.hollyvoc.simpletp;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

/**
 * Created by qianxm on 2017/12/26.
 */
public class SimpleSpout implements IRichSpout {

    private static final Logger LOG = LoggerFactory.getLogger(com.hollycrm.hollyvoc.batchexample.SimpleSpout.class);
    private Random rand;
    private int batchSize = 100;
    private SpoutOutputCollector collector;


    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        this.collector = collector;
    }

    @Override
    public void close() {

    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void nextTuple() {
        for (int i = 0; i < batchSize; i++) {
            long value = rand.nextInt(10);
            collector.emit("stream-id",new Values(value));
        }
    }

    @Override
    public void ack(Object msgId) {

    }

    @Override
    public void fail(Object msgId) {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(
                "stream-id",
                new Fields("value")
        );
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
