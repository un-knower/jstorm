package com.hollycrm.hollyvoc.kryo.bolt;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.alibaba.jstorm.kafka.KafkaConstant;
import com.hollycrm.hollyvoc.constant.ByteUtil;
import com.hollycrm.hollyvoc.constant.TopoConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by qianxm on 2018/1/5.
 * 将kafka spout中的数据进行处理发送到下游。
 */
public class DataProcess implements IBasicBolt {

    public final static String NAME = "data-processing-bolt";

    private static Logger logger = LoggerFactory.getLogger(DataProcess.class);

    // 统计数据量
    private AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void prepare(Map stormConf, TopologyContext context) {

    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {

        // 处理指定数据流的数据
        if(TopoConstant.TOPOLOGY_STREAM_TXT_ID.equals(input.getSourceStreamId())) {
            try {
                // 1. 将数据转换成对象
//                String rowKey;
                // rowkey
                String key = ByteUtil.getStringFromByteArray(input.getBinaryByField(KafkaConstant.KEY));
                // 结构数据json格式
                String value = ByteUtil.getStringFromByteArray(input.getBinaryByField(KafkaConstant.VALUE));

                // 2. 将数据对象传递给下游

            } catch (Exception e) {
                logger.error(" 数据转换异常", e);
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
