package com.hollycrm.hollyvoc.hbase;

import backtype.storm.task.OutputCollector;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.hollycrm.hollyvoc.constant.TopoConstant;
import com.hollycrm.hollyvoc.constant.Constant;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/7/11.
 * 批量写入
 */
public class HBaseHelper {
    private static Logger logger = LoggerFactory.getLogger(HBaseHelper.class);

    private List<Tuple> tuples = null; // 接受到的数据

    private List<Values> datas = null;
    private List<Put> puts = null;

    private int batchSize = 1000;

    private OutputCollector collector = null;
    private Connection connection;

    private static byte[] infoFamily = Bytes.toBytes(CUST_INFO_H_FAMILY), // info 列族
            txtFamily = Bytes.toBytes(CUST_TXT_H_FAMILY), // txt列族
            infoQua = Bytes.toBytes(CUST_INFO_H_QUA), // info 列名
            userTxtQua = Bytes.toBytes(CUST_USER_TXT_H_QUE), // txt 用户通话列名
            agentTxtQua = Bytes.toBytes(CUST_AGENT_TXT_H_QUA), // txt 坐席通话内容
            allTxtQua = Bytes.toBytes(CUST_ALL_TXT_H_QUA); // txt 所有通话内容

    public HBaseHelper(int batchSize, OutputCollector collector, Connection connection) {
        this.tuples = new ArrayList<>();
        this.datas = new ArrayList<>();
        if (batchSize > 0) this.batchSize = batchSize;
        this.collector = collector;
        this.connection = connection;
    }

    /**
     * kafka接收到的消息添加导数据列表
     * @param input 数据
     */
    public void add(Tuple input) {
        tuples.add(input);
        String rowKey = input.getStringByField(DEC_ROW_KEY);
        String basicInfo = input.getStringByField(DEC_BASIC_INFO);
        String agentTxt = input.getStringByField(DEC_AGENT_TXT);
        String userTxt = input.getStringByField(DEC_USER_TXT);
        String prov = input.getStringByField(DEC_PROVINCE);
        String allTxt = input.getStringByField(TopoConstant.DEC_ALL_TXT);
        Put put = transfer2Put(rowKey, basicInfo, allTxt, agentTxt, userTxt);
        Values value = new Values(rowKey, prov, basicInfo, agentTxt, userTxt, allTxt);
        puts.add(put);
        if (tuples.size() == batchSize) {
            bulkInsert(puts,value);
            datas.clear();
            ack();
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

    public void ack() {
        for (int i = 0, len = tuples.size(); i < len; i++) {
            collector.ack(tuples.get(i));
        }
        tuples.clear();
    }

    public void fail(Exception e) {
        collector.reportError(e);
        for (int i = 0, len = tuples.size(); i < len; i++) {
            collector.fail(tuples.get(i));
        }
        tuples.clear();
        datas.clear();
    }

    public void bulkInsert(List<Put> datas,Values value) {
        if (null == datas || datas.size() == 0) return;
        try (Table table = connection.getTable(TableName.valueOf(Constant.CUST_INFO_H_TABLE))) {
            table.put(datas); // 应采用批量保存，保存数据
            collector.emit(TOPOLOGY_STREAM_HBASE_ID, value);
        }catch (Exception e) {
            logger.error("save hbase error!", e);
        }
        logger.info("hbase insert " + datas.size() + " records finish!");
        System.out.println("hbase insert " + datas.size() + " records finish!");
    }
}
