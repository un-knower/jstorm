package com.hollycrm.hollyvoc.batch;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.FailedException;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.TupleHelpers;
import com.alibaba.jstorm.batch.BatchId;
import com.alibaba.jstorm.batch.ICommitter;
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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/7/11.
 * 保存数据到hbase
 */
public class HbaseBatchBolt implements IBasicBolt, ICommitter {

    private static Logger logger = LoggerFactory.getLogger(HbaseBatchBolt.class);
    public static final String NAME = "hbase-batch-store";
    private Connection connection;
    private static byte[] infoFamily = Bytes.toBytes(CUST_INFO_H_FAMILY),
            txtFamily = Bytes.toBytes(CUST_TXT_H_FAMILY),
            infoQua = Bytes.toBytes(CUST_INFO_H_QUA),
            userTxtQua = Bytes.toBytes(CUST_USER_TXT_H_QUE),
            agentTxtQua = Bytes.toBytes(CUST_AGENT_TXT_H_QUA),
            allTxtQua = Bytes.toBytes(CUST_ALL_TXT_H_QUA);
    private final ConcurrentMap<Long, Queue<Put>> dataMaps = new ConcurrentHashMap<>(); // 提交到hbase的数据， TODO queue-》list
    private final ConcurrentHashMap<Long,Queue<Values>> datas = new ConcurrentHashMap<>(); // 保存发送到下游的数据


    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        try {
            Configuration conf = HBaseConfiguration.create();
            conf.set(Constant.HBASE_ZK_QUORUM, ConfigUtils.getStrVal(Constant.HBASE_ZK_QUORUM));
            conf.set(Constant.HBASE_ZK_QUORUM_PORT, ConfigUtils.getStrVal(Constant.HBASE_ZK_QUORUM_PORT));
            this.connection = ConnectionFactory.createConnection(conf);
        }catch (IOException e) {
            logger.error("init hbase connection has error", e);
        }
    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
//        if("streamId".equals(input.getSourceStreamId())) {
        logger.info("getSourceStreamId  " + input.getSourceStreamId());
        logger.info("isTickTuple " +TupleHelpers.isTickTuple(input));
//        System.out.println("getSourceStreamId  " + input.getSourceStreamId());
//        System.out.println("isTickTuple " +TupleHelpers.isTickTuple(input));
        if(TupleHelpers.isTickTuple(input)) return;
        BatchId batchId = (BatchId) input.getValue(0);
        String rowKey = input.getStringByField(DEC_ROW_KEY);
        logger.info(Thread.currentThread().getName() + " rowkey " +rowKey + " batchId " +batchId);
//        System.out.println(Thread.currentThread().getName() + " rowkey " +rowKey + " batchId " +batchId);
        String basicInfo = input.getStringByField(DEC_BASIC_INFO);
        String agentTxt = input.getStringByField(DEC_AGENT_TXT);
        String userTxt = input.getStringByField(DEC_USER_TXT);
        String prov = input.getStringByField(DEC_PROVINCE);
        String allTxt = input.getStringByField(TopoConstant.DEC_ALL_TXT);
        Put put = transfer2Put(rowKey, basicInfo, allTxt, agentTxt, userTxt);
        Queue<Put> queue =  dataMaps.computeIfAbsent(batchId.getId(), (k) -> new ConcurrentLinkedQueue<>());
        queue.add(put);
        Queue<Values> values = datas.computeIfAbsent(batchId.getId(),(k)->new ConcurrentLinkedQueue<Values>());
        values.add(new Values(batchId, rowKey, prov, basicInfo, agentTxt, userTxt, allTxt));
// datas.put(batchId,new Values(batchId, rowKey, prov, basicInfo, agentTxt, userTxt, allTxt));
//        collector.emit(new Values(batchId, rowKey, prov, basicInfo, agentTxt, userTxt, allTxt));
    }
    private static Put transfer2Put(String rowKey, String info, String allTxt, String agentTxt, String userTxt) {
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(infoFamily, infoQua, Bytes.toBytes(info));
        put.addColumn(txtFamily, allTxtQua, Bytes.toBytes(allTxt));
        put.addColumn(txtFamily, agentTxtQua, Bytes.toBytes(agentTxt));
        put.addColumn(txtFamily, userTxtQua, Bytes.toBytes(userTxt));
        return put;
    }
    @Override
    public void cleanup() {
        // shutdown 把数据发送给kafka，重新处理。关闭所有的访问接连
        try{
            connection.close();
        } catch (Exception e) {
            logger.error(" clean up err", e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(DEC_BATCH_ID, DEC_ROW_KEY, DEC_PROVINCE, DEC_DAY,
                DEC_BASIC_INFO, DEC_USER_TXT, DEC_AGENT_TXT, DEC_ALL_TXT));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    @Override
    public byte[] commit(BatchId id) throws FailedException {
        Queue<Put> queue = dataMaps.get(id.getId());
//
        if(queue != null && queue.size() > 0) {
//            System.out.println("queue size ： " +queue.size());
            try (Table table = connection.getTable(TableName.valueOf(Constant.CUST_INFO_H_TABLE))) {
                table.put( new ArrayList<>(queue));
                // todo 测试提交异常  如果出现异常会一直重试
//                String[] s1=null;
//                String s2 = s1[1];
                logger.info(Thread.currentThread().getName() + " batchId: " +id.getId() + " commit to hbase " + queue.size());
                System.out.println(Thread.currentThread().getName() + " batchId: " +id.getId() + " commit to hbase " + queue.size());

            }catch (IOException e){
                logger.error("save hbase, batch id is " + id.getId(), e);
                // todo 发送给err下游
                System.out.println("save hbase, batch id is " + id.getId() + e);
//                return "fail".getBytes();
            }
        }
        return "success".getBytes();
    }

    @Override
    public void revert(BatchId id, byte[] commitResult) {

        System.out.println(id + "  revert =================== " + new String(commitResult));

    }
}
