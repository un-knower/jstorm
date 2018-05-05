package com.hollycrm.hollyvoc.qc.topology;

import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.SpoutDeclarer;
import backtype.storm.topology.TopologyBuilder;
import com.alibaba.jstorm.kafka.KafkaSpout;
import com.alibaba.jstorm.kafka.KafkaSpoutConfig;
import com.alibaba.jstorm.utils.JStormUtils;
import com.hollycrm.hollyvoc.qc.bolt.DataProcess;
import com.hollycrm.hollyvoc.qc.bolt.OracleBolt;
import com.hollycrm.hollyvoc.qc.bolt.QualityBolt;
import com.hollycrm.hollyvoc.qc.bolt.RuleBolt;
import com.hollycrm.hollyvoc.qc.kryobolt.DataParseBolt;
import com.hollycrm.hollyvoc.qc.kryobolt.KryoQcBolt;
import com.hollycrm.util.config.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

import static com.hollycrm.hollyvoc.qc.QCConstant.*;

/**
 * Created by qianxm on 2017/9/12.
 * 质检拓扑关系
 */
public class QcTopology {

    private static Logger logger = LoggerFactory.getLogger(QcTopology.class);
    public final static String TOPOLOGY_NAME = "record-qc-topology";

    /**
     * 定义拓扑
     * @param topology 拓扑对象
     * @param conf     配置
     */
    public static void defineTopology(TopologyBuilder topology, Map<String, Object> conf) {
        System.out.println(" defineTopology .....");

        // kafka 配置
        Properties props = ConfigUtils.getProp("/application.properties");
        KafkaSpoutConfig spoutConfig = new KafkaSpoutConfig(props);
//        spoutConfig.s
        conf.put("group.id", "kafka-topology");
        spoutConfig.configure(conf);
        System.out.println("kafka.group.id" + conf.get("kafka.group.id"));
        // 原始数据 spout
        SpoutDeclarer txtSpout = topology.setSpout(QC_SPOUT, new KafkaSpout(spoutConfig, TOPOLOGY_STREAM_QC_ID),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_SPOUT, 1))); // spout 名称、spout、并行数量

        BoltDeclarer dataprocess = topology.setBolt(DataProcess.NAME, new DataProcess(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_SPOUT, 1)));
        // 定义质检 bolt
        BoltDeclarer qualityBolt = topology.setBolt(QualityBolt.NAME, new QualityBolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_QC, 1)));
        // 保存质检 bolt
        BoltDeclarer oracleBolt = topology.setBolt(OracleBolt.NAME, new OracleBolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_ORACLE, 1)));
//        BoltDeclarer error = topology.setBolt(ErrProducerBolt.NAME, new ErrProducerBolt(),
//                ConfigUtils.getIntVal(conf,TOPOLOGY_PARALLEL_ERROR, 1));

        BoltDeclarer ruleQc = topology.setBolt(RuleBolt.NAME, new RuleBolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_QC, 1)));

//        dataprocess.localOrShuffleGrouping(QC_SPOUT, TOPOLOGY_STREAM_QC_ID);

//        // 质检 输入：hbase数据流, 输出：质检结果
//        qualityBolt.localOrShuffleGrouping(DataProcess.NAME, TOPOLOGY_STREAM_QC_ID);
//        // 质检异常
////        error.localOrShuffleGrouping(QualityBolt.NAME, TOPOLOGY_STREAM_QC_ERR_ID);
//        // 存储Oracle 输入：质检数据流  输出：无
//        oracleBolt.localOrShuffleGrouping(QualityBolt.NAME, TOPOLOGY_ORACLE_ID);
//        // 存储异常
//        oracleBolt.localOrShuffleGrouping(DataProcess.NAME, TOPOLOGY_ORACLE_ID);

//        error.localOrShuffleGrouping(OracleBolt.NAME, TOPOLOGY_ORACLE_ERR_ID);
//        ruleQc.localOrShuffleGrouping(DataProcess.NAME, TOPOLOGY_STREAM_QC_ID);


        // 自定义序列化参数
        BoltDeclarer kyData = topology.setBolt(DataParseBolt.NAME, new DataParseBolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_QC, 1)));
        BoltDeclarer kyqc = topology.setBolt(KryoQcBolt.NAME, new KryoQcBolt(),
                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_QC, 1)));
        kyData.localOrShuffleGrouping(QC_SPOUT, TOPOLOGY_STREAM_QC_ID);
        kyqc.localOrShuffleGrouping(DataParseBolt.NAME, TOPOLOGY_STREAM_QC_ID);


    }
}
