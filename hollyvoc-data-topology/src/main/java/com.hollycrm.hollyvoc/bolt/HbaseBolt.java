package com.hollycrm.hollyvoc.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.hollycrm.hollyvoc.constant.TopoConstant;
import com.hollycrm.util.config.ConfigUtils;
import com.hollycrm.hollyvoc.constant.Constant;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.*;
import static org.apache.hadoop.hbase.CellUtil.cloneValue;


/**
 * Created by qianxm on 2017/7/7.
 */
public class HbaseBolt implements IRichBolt {

    public final static String HBNAME = "hbase-bolt";

    private static Logger logger = LoggerFactory.getLogger(HbaseBolt.class);
    private Connection connection;
    private static byte[] infoFamily = Bytes.toBytes(CUST_INFO_H_FAMILY), // info 列族
            txtFamily = Bytes.toBytes(CUST_TXT_H_FAMILY), // txt列族
            infoQua = Bytes.toBytes(CUST_INFO_H_QUA), // info 列名
            userTxtQua = Bytes.toBytes(CUST_USER_TXT_H_QUE), // txt 用户通话列名
            agentTxtQua = Bytes.toBytes(CUST_AGENT_TXT_H_QUA), // txt 坐席通话内容
            allTxtQua = Bytes.toBytes(CUST_ALL_TXT_H_QUA); // txt 所有通话内容

    private int batchSize = HABSE_BATCHSIZE;
    private ConcurrentHashMap<String,List<Put>> resource; // 消息数据
    private ConcurrentHashMap<String,Long> timeMap;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    OutputCollector collector;
    private AtomicInteger counter = new AtomicInteger(0);
    private AtomicInteger emitcounter = new AtomicInteger(0);
    private ConcurrentHashMap<String,Long> index; // 每个省份发送了多少数据。
    private AtomicInteger firstcounter = new AtomicInteger(0);




    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        try {
//            logger.debug("HbaseBolt -- prepare");
            Configuration conf = HBaseConfiguration.create();
            conf.set(Constant.HBASE_ZK_QUORUM, ConfigUtils.getStrVal(Constant.HBASE_ZK_QUORUM));
            conf.set(Constant.HBASE_ZK_QUORUM_PORT, ConfigUtils.getStrVal(Constant.HBASE_ZK_QUORUM_PORT));
            this.connection = ConnectionFactory.createConnection(conf); // 链接hbase
            this.resource = new ConcurrentHashMap<>();
            this.timeMap = new ConcurrentHashMap<>();
            this.collector = collector;
            index = new ConcurrentHashMap<>();
        }catch (IOException e) {
            logger.error("init hbase connection has error", e);
            throw new RuntimeException("init hbase connection has error", e);
        }
    }

    /**
     * 接受数据并完成逻辑处理
     * @param input 数据元组
     */
    @Override
    public void execute(Tuple input) {
        String threadName = Thread.currentThread().getName();
//        System.out.println("hbase-bolt : " + Thread.currentThread().getName() + " StreamId :" + input.getSourceStreamId());
        try {
            if (TopoConstant.TOPOLOGY_STREAM_TXT_ID.equals(input.getSourceStreamId())) { // 如果没有指定id的数据传入，空消息是_tick
                counter.incrementAndGet();

                String rowKey = input.getStringByField(DEC_ROW_KEY);
                String basicInfo = input.getStringByField(DEC_BASIC_INFO);
                String agentTxt = input.getStringByField(DEC_AGENT_TXT);
                String userTxt = input.getStringByField(DEC_USER_TXT);
                String prov = input.getStringByField(DEC_PROVINCE);
                String allTxt = input.getStringByField(TopoConstant.DEC_ALL_TXT);

                // 保存到hbase 数据
                Put put = transfer2Put(rowKey, basicInfo, allTxt, agentTxt, userTxt);
                // 根据线程名称获取到保存的数据list
                resource.computeIfAbsent(threadName, list-> new ArrayList<>(ORACLE_BATCHSIZE));
                List<Put> puts = resource.get(threadName);
                puts.add(put);
                // todo 考虑kafka如果长时间接受不到数据，不足1000条的数据也需要提交，根据时间，如果两次提交时间间隔超过两小时，如果不满1000条也需要提交
                Long lastTime = timeMap.get(threadName)==null?System.currentTimeMillis():timeMap.get(threadName);
                 // 满足条件或者上次提交时间超过1秒
                if (puts.size() == HABSE_BATCHSIZE) { // 满足2000条
                    // 提交hbase
                    List<Put> temp = new ArrayList<>(puts.size());
                    temp.addAll(puts);
                    puts.clear(); // 清空数据
                    // 提交数据到hbase，并发送数据给下游，提交失败时会发送给err-bolt
                    commit(connection, temp, this.collector,emitcounter,index);
                   temp.clear(); // 提交成功后清除数据
                }
                // 最后一次处理消息的时间
                timeMap.put(threadName,System.currentTimeMillis());

            } else { // 没有数据要处理的数据时,超过5秒没有指定的数据接入，同样提交数据
                Long time = timeMap.get(threadName);
                // 只处理本线程中的数据, 超时没有接收到数据
                if(time!=null && (System.currentTimeMillis() - timeMap.get(threadName))/ 1000 > TIME_OUT) {
                    // 备份清数据
                    timeMap.remove(threadName);
                    List<Put> backupDatas = new ArrayList<>();
                    // 处理本线程就不用考虑锁的问题
                    backupDatas.addAll(resource.get(threadName));
                    resource.remove(threadName);

                    // 提交数据
                    if (backupDatas.size() > 0) {
                        commit(connection, backupDatas, collector, emitcounter, index);
                    }
                }
            }
            // 确保消息可靠性
            collector.ack(input);
        }catch (Exception e) {
//            logger.error(" hbase-bolt execute dataType error!" + input.getStringByField(DEC_ROW_KEY));
//            logger.error(" values " + input.getStringByField(DEC_BASIC_INFO));
            logger.error(" Exception: ", e);
            collector.fail(input);
        }

    }

    // 将hbase 字符串转换成bytes hbase是以子节存储的
    private static Put transfer2Put(String rowKey, String info, String allTxt, String agentTxt, String userTxt) {
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(infoFamily, infoQua, Bytes.toBytes(info));
        put.addColumn(txtFamily, allTxtQua, Bytes.toBytes(allTxt));
        put.addColumn(txtFamily, agentTxtQua, Bytes.toBytes(agentTxt));
        put.addColumn(txtFamily, userTxtQua, Bytes.toBytes(userTxt));
        return put;
    }

    private  void commit(Connection connection, List<Put> put, OutputCollector collector,
                         AtomicInteger emitcounter, ConcurrentHashMap index){
        // todo 测试表
        try (Table table = connection.getTable(TableName.valueOf(Constant.CUST_INFO_H_TEST_TABLE))){
        // 此table非线程安全 且使用完毕须关闭
//        try (Table table = connection.getTable(TableName.valueOf(Constant.CUST_INFO_H_TABLE))){
            // 保存数据
            table.put(put); // 应采用批量保存，保存数据
            logger.info("hbase insert " + put.size() + " records finish!");
            System.out.println("hbase insert " + put.size() + " records finish!");
            emitData(put, TOPOLOGY_STREAM_HBASE_ID, collector, emitcounter,index); // 保存成功后，发送给index-bolt 进行质检
//            System.out.println(Thread.currentThread().getName() +" hbase insert " + put.size() + " records finish!");
            table.close();
        }catch (IOException e) {
            logger.error("hbase--> execute has error", e);
            // 保存失败 发送给kafka
            emitData(put, TOPOLOGY_STREAM_HBASE_COMMIT_ERR_ID, collector, emitcounter, index); // 保存失败成功后，
        }

    }

    private static void emitData(List<Put> puts,String streamId, OutputCollector collector,
                                 AtomicInteger emitcounter, ConcurrentHashMap<String, Long> index){

        puts.forEach(s-> { // 保存成功后，
            String rowkey = Bytes.toString(s.getRow());
            String info = Bytes.toString(cloneValue(s.get(infoFamily, infoQua).get(0)));
            String allTxt = Bytes.toString(cloneValue(s.get(txtFamily, allTxtQua).get(0)));
            String agenTxt = Bytes.toString(cloneValue(s.get(txtFamily, agentTxtQua).get(0)));
            String  userTxt = Bytes.toString(cloneValue(s.get(txtFamily, userTxtQua).get(0)));
//            logger.info("rowkey" + rowkey);
            String prov = rowkey.replaceAll(" ","").substring(10, 12);

//            logger.info(prov);
            collector.emit(streamId, new Values(prov,rowkey,info,agenTxt,userTxt,allTxt));
            emitcounter.incrementAndGet();
            index.computeIfAbsent(prov, i -> 0L);
            Long i = index.get(prov);
            i++;
            index.put(prov, i);
        });
        logger.info("-------------"+ Thread.currentThread().getName() + " emit " +puts.size() +"-----------------");
//        System.out.println(Thread.currentThread().getName()  + " emit " +puts.size() );
}



    // 当task被shutdown后执行的动作,关闭连接
    @Override
    public void cleanup() {
        try {

            this.connection.close();
        }catch (IOException e) {
            logger.error("close hbase connection has err", e);
            e.printStackTrace();
        }
    }

    // 向下游发送数据，含义
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // 质检
        declarer.declareStream(TopoConstant.TOPOLOGY_STREAM_HBASE_ID, new Fields(
                DEC_PROVINCE, DEC_ROW_KEY, DEC_BASIC_INFO, DEC_AGENT_TXT, DEC_USER_TXT, DEC_ALL_TXT
        ));

        // 提交错误 输出格式
        declarer.declareStream(TOPOLOGY_STREAM_HBASE_COMMIT_ERR_ID,new Fields(
                DEC_PROVINCE, DEC_ROW_KEY, DEC_BASIC_INFO, DEC_AGENT_TXT, DEC_USER_TXT, DEC_ALL_TXT));
        // 格式错误
//        declarer.declareStream(TOPOLOGY_STREAM_HBASE_TYPE_ERR_ID,new Fields(
//                DEC_ROW_KEY,DEC_PROVINCE, DEC_BASIC_INFO, DEC_AGENT_TXT, DEC_USER_TXT, DEC_ALL_TXT));
    }

    // 获取本bolt的component 配置
    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }


}
