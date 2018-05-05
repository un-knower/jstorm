//package com.hollycrm.hollyvoc.app;
//
//import backtype.storm.Config;
//import backtype.storm.LocalCluster;
//import backtype.storm.StormSubmitter;
//import backtype.storm.generated.AlreadyAliveException;
//import backtype.storm.generated.InvalidTopologyException;
//import backtype.storm.topology.TopologyBuilder;
//import com.alibaba.jstorm.utils.LoadConf;
//import com.hollycrm.hollyvoc.topology.StreamTopology;
//import org.apache.commons.lang.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.hollycrm.hollyvoc.constant.TopoConstant.TOPOLOGY_NAME;
//
///**
// * Created by qianxm on 2017/8/22.
// * 动态配置入口
// */
//public class ProApplication {
//
//    private static Logger logger = LoggerFactory.getLogger(ProApplication.class);
//
//    /**
//     * 配置参数
//     */
//    private static Map conf = new HashMap<String, Object>();
//
//
//    /**
//     * 本地运行模式
//     * @param conf 配置
//     * @throws InterruptedException 异常
//     */
//    private static void runLocalMode(Map<String, Object> conf) throws InterruptedException{
//        try {
//            TopologyBuilder builder = new TopologyBuilder();
//            LocalCluster cluster = new LocalCluster();
//
//            StreamTopology.defineTopology(builder, conf);
//            logger.info("commit Topology ...");
//            System.out.println("commit Topology ...");
//            String topologyName = (String)conf.get(TOPOLOGY_NAME);
//            topologyName = StringUtils.isEmpty(topologyName)?topologyName:StreamTopology.TOPOLOGY_NAME;
//
//            cluster.submitTopology(topologyName, conf, builder.createTopology());
//
//            logger.info("commit Topology finish, sleep 600000");
//            System.out.println("commit Topology finish, sleep 600000");
//            Thread.sleep(600000);
//            cluster.killTopology(topologyName);
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
//        TopologyBuilder builder = new TopologyBuilder();
//
//        StreamTopology.defineTopology(builder, conf);
//
////        StormSubmitter.submitTopology(StreamTopology.TOPOLOGY_NAME, conf, builder.createTopology());
//       String topologyName = (String)conf.get(TOPOLOGY_NAME);
//       topologyName = StringUtils.isEmpty(topologyName)?topologyName:StreamTopology.TOPOLOGY_NAME;
//
//        StormSubmitter.submitTopology(topologyName, conf, builder.createTopology());
//    }
//
//
//
//
//
//    /**
//     * 加载配置.
//     * @param arg 配置文件
//     */
//    private static void LoadConf(String arg) {
//        if (arg.endsWith("yaml")) {
//            conf = LoadConf.LoadYaml(arg);
//        } else {
//            conf = LoadConf.LoadProperty(arg);
//        }
//    }
//
//    /**
//     *  加载本地参数.
//     * @param conf 配置
//     * @return 返回true说明加载的是本地的参数
//     */
//    private static boolean local_mode(Map conf) {
//        String mode = (String) conf.get(Config.STORM_CLUSTER_MODE);
//        if (mode != null) {
//            if (mode.equals("local")) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static void main(String[] args) {
//
//
//        if (args.length == 0) {
//            logger.error("Please input configuration file");
//            System.exit(-1);
//        }
//        // 本地测试
////        String config = ConfigUtils.class.getResource("/topology-local.yaml").getFile();
////        System.out.println(config);
//////        String config = "D:\\workspace\\holly-data-stream\\hollyvoc-data-topology\\target\\classes\\topology-local.yaml";
////        if(args!=null && args.length > 0){
////            config = args[0];
////
////        }
//        String config = args[0];
//        // 加载配置文件，判断是本地模式还是集群模式
//        LoadConf(config);
//
//        try {
//            if (local_mode(conf)) {
//                runLocalMode(conf);
//            } else {
//                runRemoteMode(conf);
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
