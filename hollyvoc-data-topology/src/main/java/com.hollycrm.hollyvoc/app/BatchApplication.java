//package com.hollycrm.hollyvoc.app;
//
//import backtype.storm.LocalCluster;
//import backtype.storm.StormSubmitter;
//import backtype.storm.generated.AlreadyAliveException;
//import backtype.storm.generated.InvalidTopologyException;
//import backtype.storm.topology.TopologyBuilder;
//import com.alibaba.jstorm.batch.BatchTopologyBuilder;
//import com.hollycrm.hollyvoc.batch.BatchTopology;
//import com.hollycrm.util.config.ConfigUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Map;
//
///**
// * Created by qianxm on 2017/7/17.
// */
//public class BatchApplication {
//    private static Logger logger = LoggerFactory.getLogger(Application.class);
//
//    /**
//     * 本地运行模式
//     * @param conf 配置
//     * @throws InterruptedException 异常
//     */
//    private static void runLocalMode(Map<String, Object> conf) throws InterruptedException{
//        try {
//            // 定义topoloygy
//            BatchTopologyBuilder builder = new BatchTopologyBuilder("batch-topology");
//            LocalCluster cluster = new LocalCluster();
//            // 构建拓扑结构
//            TopologyBuilder topologyBuilder = BatchTopology.defineTopology(builder);
//
//            logger.info("commit Topology ...");
//            System.out.println("commit Topology ...");
////
//            cluster.submitTopology(BatchTopology.TOPOLOGY_NAME, conf, topologyBuilder.createTopology());
//
//            logger.info("commit Topology finish, sleep 600000");
//            System.out.println("commit Topology finish, sleep 600000");
//            Thread.sleep(600000);
//            cluster.killTopology(BatchTopology.TOPOLOGY_NAME);
//            cluster.shutdown();
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 远程运行模式
//     * @param conf 配置
//     * @throws AlreadyAliveException
//     * @throws InvalidTopologyException
//     */
//    private static void runRemoteMode(Map<String, Object> conf) throws AlreadyAliveException, InvalidTopologyException {
//        BatchTopologyBuilder builder = new BatchTopologyBuilder("batch-topology");
//        TopologyBuilder topologyBuilder = BatchTopology.defineTopology(builder);
//        StormSubmitter.submitTopology(BatchTopology.TOPOLOGY_NAME, conf, topologyBuilder.createTopology());
//
//
//    }
//
//    public static void main(String[] args) {
//
//        try {
//            if (args !=null && args.length >0 &&"local".equals(args[0])) {
//                runLocalMode(ConfigUtils.prop2Map("/topology-local.properties"));
//            } else {
//                runRemoteMode(ConfigUtils.prop2Map("/topology-remote.properties"));
//            }
//        }catch (InterruptedException e) {
//            logger.error("InterruptedException ", e);
//        }
//        catch (AlreadyAliveException e) {
//            logger.error("AlreadyAliveException ", e);
//            e.printStackTrace();
//        }catch (InvalidTopologyException e) {
//            logger.error("InvalidTopologyException ", e);
//            e.printStackTrace();
//        }
//    }
//}
