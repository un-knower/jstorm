package com.hollycrm.hollyvoc.simpletp;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.BoltDeclarer;
import backtype.storm.topology.SpoutDeclarer;
import backtype.storm.topology.TopologyBuilder;
import com.alibaba.jstorm.utils.JStormUtils;
import com.alibaba.jstorm.utils.LoadConf;
import com.hollycrm.util.config.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qianxm on 2017/12/26.
 */
public class SimpleTopology {
    // 拓扑名称
    private static String topologyName = "SimpleTopology";

    private static Logger logger = LoggerFactory.getLogger(SimpleTopology.class);


    /**
     * 定义拓扑
     *
     * @param topology 拓扑对象
     * @param conf     配置
     */
    public static void defineTopology(TopologyBuilder topology, Map<String, Object> conf) {

        TopologyBuilder topologyBuilder = new TopologyBuilder();

        int spoutParallel = JStormUtils.parseInt(conf.get("topology.spout.parallel"), 1);

        // 声明spout
        SpoutDeclarer spoutDeclarer = topologyBuilder.setSpout("simple-spout",
                new SimpleSpout(), spoutParallel);

        int boltParallel = JStormUtils.parseInt(conf.get("topology.bolt.parallel"), 2);
        // 声明bolt
        BoltDeclarer bolt = topologyBuilder.setBolt(SimpleBolt.NAME, new SimpleBolt(), boltParallel);

        bolt.localOrShuffleGrouping("simple-spout","stream-id");

    }

    /**
     * 配置参数
     */
    private static Map conf = new HashMap<String, Object>();

    /**
     * 本地运行模式
     * @param conf 配置
     * @throws InterruptedException 异常
     */
    private static void runLocalMode(Map<String, Object> conf) throws InterruptedException{
        try {
            TopologyBuilder builder = new TopologyBuilder();
            LocalCluster cluster = new LocalCluster();
            SimpleTopology.defineTopology(builder, conf);
            logger.info("commit Topology ...");

            cluster.submitTopology(topologyName, conf, builder.createTopology());
            logger.info("commit Topology finish, sleep 600000");
            Thread.sleep(600000);
            cluster.killTopology(topologyName);
            cluster.shutdown();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 远程运行模式
     * @param conf 配置
     * @throws AlreadyAliveException
     * @throws InvalidTopologyException
     */
    private static void runRemoteMode(Map<String, Object> conf) throws AlreadyAliveException, InvalidTopologyException {
        TopologyBuilder builder = new TopologyBuilder();

        SimpleTopology.defineTopology(builder, conf);

        StormSubmitter.submitTopology(topologyName, conf, builder.createTopology());
    }





    /**
     * 加载配置.
     * @param arg 配置文件
     */
    private static void LoadConf(String arg) {
        if (arg.endsWith("yaml")) {
            conf = LoadConf.LoadYaml(arg);
        } else {
            conf = LoadConf.LoadProperty(arg);
        }
    }

    /**
     *  加载本地参数.
     * @param conf 配置
     * @return 返回true说明加载的是本地的参数
     */
    private static boolean local_mode(Map conf) {
        String mode = (String) conf.get(Config.STORM_CLUSTER_MODE);
        if (mode != null) {
            if (mode.equals("local")) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        // 本地测试
        String config = ConfigUtils.class.getResource("/topology-local.yaml").getFile();

        // 集群配置
//        if (args.length == 0) {
//            logger.error("Please input configuration file");
//            System.exit(-1);
//        }
//        String config = args[0];
        // 加载配置文件，判断是本地模式还是集群模式
        LoadConf(config);

        try {
            if (local_mode(conf)) {
                System.out.println(" loading local ...");
                logger.info(" loading local ... ");
                runLocalMode(conf);
            } else {
                runRemoteMode(conf);
            }
        }catch (InterruptedException e) {
            logger.error("InterruptedException ", e);
        }
        catch (AlreadyAliveException e) {
            logger.error("AlreadyAliveException ", e);
            e.printStackTrace();
        }catch (InvalidTopologyException e) {
            logger.error("InvalidTopologyException ", e);
            e.printStackTrace();
        }
    }
}
