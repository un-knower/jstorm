//package com.hollycrm.hollyvoc.topology;
//
//import backtype.storm.topology.BoltDeclarer;
//import backtype.storm.topology.SpoutDeclarer;
//import backtype.storm.topology.TopologyBuilder;
//import backtype.storm.tuple.Fields;
//import com.alibaba.jstorm.utils.JStormUtils;
//import com.hollycrm.hollyvoc.bolt.*;
//import com.hollycrm.hollyvoc.spout.TxtSpout;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Map;
//
//import static com.hollycrm.hollyvoc.bolt.HbaseBolt.HBNAME;
//import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
//import static com.hollycrm.hollyvoc.spout.TxtSpout.NAME;
//
///**
// * Created by qianxm on 2017/7/6.
// * 流式处理拓扑：订阅kafka，数据由kafka流入->保存到Hbase -> 索引、质检、学习新词
// * 质检暂时不放入此拓扑
// */
//public class StreamTopology {
//
//    private static Logger logger = LoggerFactory.getLogger(StreamTopology.class);
//    public final static String TOPOLOGY_NAME = "record-basic-topology";
//
//    /**
//     * 定义拓扑
//     * @param topology 拓扑对象
//     * @param conf 配置
//     */
//    public static void defineTopology(TopologyBuilder topology, Map<String, Object> conf){
//        System.out.println(" defineTopology .....");
//        // 原始数据 spout
//        SpoutDeclarer txtSpout = topology.setSpout(NAME, new TxtSpout(),
//                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_SPOUT,1))); // spout 名称、spout、并行数量
//        // 定义 hbase bolt
//        BoltDeclarer hbaseBolt = topology.setBolt(HBNAME, new HbaseBolt(),
//                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_BOLT_HBASE, 1)));
//        // 定义索引 单条索引 bolt
////        BoltDeclarer indexBolt = topology.setBolt(INDEXNAME, new IndexBolt(),
////                ConfigUtils.getIntVal(conf, TOPOLOGY_PARALLEL_BOLT_INDEX, 1));
//
//        // 批量索引
//        BoltDeclarer indexBatchBolt = topology.setBolt(IndexBolt.NAME, new IndexBolt(),
//                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_BOLT_INDEX, 2)));
//
//        // 定义质检 bolt
////        BoltDeclarer qualityBolt = topology.setBolt(QualityBolt.NAME, new QualityBolt(),
////                ConfigUtils.getIntVal(conf, TOPOLOGY_PARALLEL_QC, 1));
////        // 保存质检 bolt
////        BoltDeclarer oracleBolt = topology.setBolt(OracleBolt.NAME, new OracleBolt(),
////                ConfigUtils.getIntVal(conf, TOPOLOGY_PARALLEL_ORACLE, 1));
//
//        // 将异常数据发送给kafka
//        BoltDeclarer errorBolt = topology.setBolt(ErrProducerBolt.NAME, new ErrProducerBolt(),
//                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_ERROR, 1)));
//        // 新词
//        BoltDeclarer newWordBolt = topology.setBolt(NewWordBolt.NAME, new NewWordBolt(),
//                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_NW, 1)));
//        // 新词存放到redis
//        BoltDeclarer nw2RedisBolt = topology.setBolt(NW2RedisBolt.NAME, new NW2RedisBolt(),
//                JStormUtils.parseInt(conf.getOrDefault(TOPOLOGY_PARALLEL_NW_REDIS, 1)));
//
//
//        // 构建拓扑图, 注意如果并行大于1，时不能使用allGrouping,这种分发机制回分发给每个并行的bolt
////        hbaseBolt.allGrouping(TxtSpout.NAME, TOPOLOGY_STREAM_TXT_ID);
//        // 本地或随机分配
//        hbaseBolt.localOrShuffleGrouping(TxtSpout.NAME, TOPOLOGY_STREAM_TXT_ID);
////        // 如果hbase处理失败将消息发送给kafka
//        errorBolt.localOrShuffleGrouping(HbaseBolt.HBNAME,TOPOLOGY_STREAM_HBASE_COMMIT_ERR_ID);
////         // 索引 接入hbase数据流 无输出
//        //  类似SQL中的group by， 保证相同的Key的数据会发送到相同的task， 原理是 对某个或几个字段做hash，然后用hash结果求模得出目标taskId
//        indexBatchBolt.fieldsGrouping(HbaseBolt.HBNAME,TOPOLOGY_STREAM_HBASE_ID,new Fields(DEC_PROVINCE));
//
//        // 索引异常数据
//        indexBatchBolt.localOrShuffleGrouping(TxtSpout.NAME,TOPOLOGY_STREAM_HBASE_ID);
//
//        // 如果索引失败，将消息发送给kafka
//        errorBolt.localOrShuffleGrouping(IndexBolt.NAME,TOPOLOGY_STREAM_INDEX_ERR_ID);
////        // 存储原始文件 输入：原始文本 无输出
//        // 质检 输入：hbase数据流, 输出：质检结果
////        qualityBolt.localOrShuffleGrouping(HbaseBolt.HBNAME, TOPOLOGY_STREAM_HBASE_ID);
////        // 存储Oracle 输入：质检数据流  输出：无
////        oracleBolt.localOrShuffleGrouping(QualityBolt.NAME, TOPOLOGY_STREAM_QC_ID);
////        // 存储异常
////        oracleBolt.localOrShuffleGrouping(TxtSpout.NAME, TOPOLOGY_STREAM_QC_ID);
//
//        // hbase 保存成功后，进行新词学习
//        newWordBolt.localOrShuffleGrouping(HbaseBolt.HBNAME,TOPOLOGY_STREAM_HBASE_ID);
//
//        // 异常消息发送到kafka
//        errorBolt.localOrShuffleGrouping(NewWordBolt.NAME, TOPOLOGY_STREAM_NW_ERR_ID);
//
//        // 异常topic数据，进行新词学习
//        newWordBolt.localOrShuffleGrouping(TxtSpout.NAME,TOPOLOGY_STREAM_NW_ERR_ID);
//
//        // 学习新词保存到redis
//        nw2RedisBolt.localOrShuffleGrouping(NewWordBolt.NAME,TOPOLOGY_STREAM_NW_ID);
//
//        // 异常消息发送到kafka
//        errorBolt.localOrShuffleGrouping(NW2RedisBolt.NAME, TOPOLOGY_STREAM_NW2REDIS_ERR_ID);
//
//        // 处理spout发送的异常topic中数据
//        nw2RedisBolt.localOrShuffleGrouping(TxtSpout.NAME, TOPOLOGY_STREAM_NW2REDIS_ERR_ID);
//
//
//    }
//
//
//    public static void main(String[] args) {
//
//        System.out.println(get("1"));
//
//    }
//
//    private static String get(String id){
//        if(id != null)
//            return id.toString() + "-";
//        return id;
//    }
//
//
//}
