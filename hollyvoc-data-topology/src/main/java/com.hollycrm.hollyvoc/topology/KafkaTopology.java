package com.hollycrm.hollyvoc.topology;

import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.SpoutDeclarer;
import backtype.storm.topology.TopologyBuilder;
import com.alibaba.jstorm.kafka.KafkaSpout;
import com.alibaba.jstorm.kafka.KafkaSpoutConfig;
import com.alibaba.jstorm.utils.JStormUtils;
import com.hollycrm.hollyvoc.bolt.*;
import com.hollycrm.util.config.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static com.hollycrm.hollyvoc.bolt.HbaseBolt.HBNAME;
import static com.hollycrm.hollyvoc.constant.TopoConstant.*;

/**
 * Created by qianxm on 2017/8/28.
 * 流式处理-创建索引、新词学习拓扑关系
 */
public class KafkaTopology {

    private static Logger logger = LoggerFactory.getLogger(KafkaTopology.class);
    public final static String TOPOLOGY_NAME = "record-kafka-topology";

    /**
     * 定义拓扑
     *
     * @param topology 拓扑对象
     * @param conf     配置
     */
    public static void defineTopology(TopologyBuilder topology, Map<String, Object> conf) {
        System.out.println(" defineTopology .....");

//        Properties props = ConfigUtils.getProp("/topology-remote.yaml");
        // kafka 配置
        Properties props = ConfigUtils.getProp("/application.properties");
        KafkaSpoutConfig spoutConfig = new KafkaSpoutConfig(props);
//        spoutConfig.s
        conf.put("group.id", "kafka-topology");
        spoutConfig.configure(conf);
        System.out.println("kafka.group.id" + conf.get("kafka.group.id"));
        // 原始数据 spout
        SpoutDeclarer txtSpout = topology.setSpout(KAFKA_SPOUT, new KafkaSpout(spoutConfig, TOPOLOGY_STREAM_TXT_ID),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_SPOUT, 1))); // spout 名称、spout、并行数量

        BoltDeclarer dataprocess = topology.setBolt(DataProcess.NAME, new DataProcess(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_SPOUT, 1)));

         //定义 hbase bolt
        BoltDeclarer hbaseBolt = topology.setBolt(HBNAME, new HbaseBolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_BOLT_HBASE, 1)));

        // 批量索引
        BoltDeclarer indexBatchBolt = topology.setBolt(IndexThreadbolt.NAME, new IndexThreadbolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_BOLT_INDEX, 2)));
//        BoltDeclarer indexBatchBolt = topology.setBolt(IndexBolt.NAME, new IndexBolt(),
//                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_BOLT_INDEX, 1)));

        // 将异常数据发送给kafka
        BoltDeclarer errorBolt = topology.setBolt(ErrProducerBolt.NAME, new ErrProducerBolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_ERROR, 1)));
        BoltDeclarer hbaseProducer = topology.setBolt(HbaseProducerBolt.NAME, new HbaseProducerBolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_BOLT_HBASE, 1)));
        // 新词
        BoltDeclarer newWordBolt = topology.setBolt(NewWordBolt.NAME, new NewWordBolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_NW, 1)));
        // 新词存放到redis
        BoltDeclarer nw2RedisBolt = topology.setBolt(NW2RedisBolt.NAME, new NW2RedisBolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_NW_REDIS, 1)));


        dataprocess.localOrShuffleGrouping(KAFKA_SPOUT, TOPOLOGY_STREAM_TXT_ID);
        // 构建拓扑图, 注意如果并行大于1，时不能使用allGrouping,这种分发机制回分发给每个并行的bolt
//        hbaseBolt.allGrouping(TxtSpout.NAME, TOPOLOGY_STREAM_TXT_ID);
        //本地或随机分配
        hbaseBolt.localOrShuffleGrouping(DataProcess.NAME, TOPOLOGY_STREAM_TXT_ID);
//
//        //todo ceshi 将保存成功的数据发送到kafka中
        hbaseProducer.localOrShuffleGrouping(HbaseBolt.HBNAME, TOPOLOGY_STREAM_HBASE_ID);
//
//         // 如果hbase处理失败将消息发送给kafka
        errorBolt.localOrShuffleGrouping(HbaseBolt.HBNAME, TOPOLOGY_STREAM_HBASE_COMMIT_ERR_ID);
         // 索引 接入hbase数据流 无输出
        //  类似SQL中的group by， 保证相同的Key的数据会发送到相同的task， 原理是 对某个或几个字段做hash，然后用hash结果求模得出目标taskId
//        indexBatchBolt.fieldsGrouping(HbaseBolt.HBNAME, TOPOLOGY_STREAM_HBASE_ID, new Fields(DEC_PROVINCE));
//        // 索引异常数据
//        indexBatchBolt.fieldsGrouping(DataProcess.NAME, TOPOLOGY_STREAM_HBASE_ID,new Fields(DEC_PROVINCE));

        indexBatchBolt.localOrShuffleGrouping(HbaseBolt.HBNAME, TOPOLOGY_STREAM_HBASE_ID);
        indexBatchBolt.localOrShuffleGrouping(DataProcess.NAME, TOPOLOGY_STREAM_HBASE_ID);
        // 如果索引失败，将消息发送给kafka
        errorBolt.localOrShuffleGrouping(IndexThreadbolt.NAME, TOPOLOGY_STREAM_INDEX_ERR_ID);

        // hbase 保存成功后，进行新词学习
//        newWordBolt.localOrShuffleGrouping(HbaseBolt.HBNAME, TOPOLOGY_STREAM_HBASE_ID);
//        // 异常消息发送到kafka
//        errorBolt.localOrShuffleGrouping(NewWordBolt.NAME, TOPOLOGY_STREAM_NW_ERR_ID);

         // 异常topic数据，进行新词学习
//        newWordBolt.localOrShuffleGrouping(DataProcess.NAME, TOPOLOGY_STREAM_NW_ERR_ID);
//
//        // 学习新词保存到redis
//        nw2RedisBolt.localOrShuffleGrouping(NewWordBolt.NAME, TOPOLOGY_STREAM_NW_ID);

        //异常消息发送到kafka
//        errorBolt.localOrShuffleGrouping(NW2RedisBolt.NAME, TOPOLOGY_STREAM_NW2REDIS_ERR_ID);

        // 处理spout发送的异常topic中数据
//        nw2RedisBolt.localOrShuffleGrouping(DataProcess.NAME, TOPOLOGY_STREAM_NW2REDIS_ERR_ID);
    }
}
