package com.hollycrm.hollyvoc.qc.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.hollycrm.kafka.producer.DataProducer;
import com.hollycrm.util.config.ConfigUtils;
import com.hollycrm.hollyvoc.constant.Constant;
import com.hollyvoc.helper.jdbc.JdbcHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shade.storm.org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.hollycrm.hollyvoc.qc.QCConstant.*;
import static com.hollycrm.hollyvoc.constant.ConstUtils.javaId;
import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/7/7.
 * 将质检数据保存到oracle,如果提交失败发送给kafka
 */
public class OracleBolt implements IRichBolt{

    public static final String NAME = "oracle-bolt";
    private static Logger logger = LoggerFactory.getLogger(OracleBolt.class);
    private ConcurrentHashMap<String,List<String[]>> resource; // 消息数据
    private ConcurrentHashMap<String,Long> timeMap;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private DataProducer producer;
    private JdbcHelper jdbc;
    private OutputCollector collector;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        try {
            this.resource = new ConcurrentHashMap<>();
            this.timeMap = new ConcurrentHashMap<>();
            Properties prop = ConfigUtils.getProp("/producer.properties");
            this.producer = new DataProducer(QC_ERR_TOPIC, prop);
            this.jdbc = JdbcHelper.getInstance();
            this.collector = collector;
        } catch (Exception e) {
            logger.error(" init oracle error! ", e);
        }
    }

    @Override
    public void execute(Tuple tuple) {
        String threadName = Thread.currentThread().getName();

        // 将接受到的数据批量保存
        if(TOPOLOGY_ORACLE_ID.equals(tuple.getSourceStreamId())){
            System.out.println("-----------oracle-bolt-------------------");
            try {
                String qcItemId = tuple.getStringByField(qcId);
                logger.info(" oracle-bolt qcItemId " + qcItemId);
                String itemName = tuple.getStringByField(qcName);
                String rowkey = tuple.getStringByField(id);
                String type = tuple.getStringByField(qcType);
                String content = tuple.getStringByField(qcTxtContent);
                String matchword = tuple.getStringByField(qcMatchWord);
                String prov = tuple.getStringByField(province);
                String qcCustBrand = tuple.getStringByField(custBrand);
                String satisfication = tuple.getStringByField(Constant.satisfication);
                String businessType = tuple.getStringByField(Constant.businessType);
                String silenceLength = tuple.getStringByField(Constant.silenceLength);
                String userCode = tuple.getStringByField(Constant.userCode);
                String caller = tuple.getStringByField(Constant.caller);
                String callee = tuple.getStringByField(Constant.callee);
                String acceptTime = tuple.getStringByField(Constant.acceptTime);
                timeMap.put(threadName, System.currentTimeMillis());
                // 如果list为空，则初始化
                resource.computeIfAbsent(threadName, list -> new ArrayList<>(ORACLE_BATCHSIZE));
                List<String[]> puts = resource.get(threadName);

                puts.add(new String[]{javaId(), qcItemId, itemName, rowkey, type, content, matchword, prov, qcCustBrand
                        , satisfication, businessType, silenceLength, userCode, acceptTime, caller, callee});
                List<String[]> temp = new ArrayList<>();

                try {
                    // 满足条件
                    if (puts.size() == ORACLE_BATCHSIZE) {
                        // 每1000条提交一次
                        temp.addAll(puts);
                        temp.addAll(temp);
                        puts.clear();

                        boolean reslut = jdbc.batchInsert(INSERT_SQL, temp);
                        System.out.println(reslut);
                        System.out.println("oracle-bolt------save qc -----" + temp.size());
                        if (!reslut) {
                            System.out.println(" save item err! ");
                            // 保存失败需要把数据发送给kafka
                            sendToKafka(temp);
                            temp.clear();
                        } else {
                            System.out.println("保存成功！");
                        }
                    }
                    long lastCommitTime = System.currentTimeMillis();
                    timeMap.put(threadName, lastCommitTime); // 更新提交时间
                    collector.ack(tuple);
                } catch (Exception e) {
                    System.out.println(" save item err! " + e);
                    // 保存失败需要把数据发送给kafka
                    sendToKafka(temp);
                }
            } catch (Exception e) {
                logger.info(" save data error!", e);
                collector.fail(tuple);
            }

        } else {

            // 超时提交超过2秒没有提交的数据，有数据也需要提交
            Long timeout = timeMap.get(threadName);
            try {
                if (timeout != null && ((System.currentTimeMillis() - timeout) / 1000) > TIME_OUT) {
                    timeMap.remove(threadName);
                    List<String[]> data = resource.get(threadName);
                    if (data.size() > 0) {

                        List<String[]> temp = new ArrayList<>(data.size());
                        temp.addAll(data);
                        resource.remove(threadName);
                        boolean reslut = false;
                        try {
                            reslut = jdbc.batchInsert(INSERT_SQL, temp);
                            logger.info(" commit " + temp.size());
                            System.out.println("oracle-bolt------timer save qc -----" + temp.size());
                        } catch (Exception e) {
                            //  发送给kafka
                            sendToKafka(temp);
                            logger.error(" save Err! ", e);
                            System.out.println(" save Err! " + e);
                        }

                        if (!reslut) {
                            System.out.println(" save item err! ");
                            // 保存失败需要把数据发送给kafka
                            sendToKafka(temp);
                        }
                        System.out.println("oracle-bolt" + threadName + " clean  " + temp.size() + " " + temp.toString());
                        logger.info(threadName + " clean  " + temp.size());
                    }

                }

            } catch (Exception e) {
                logger.error(" timeout commnit data error! ", e);
            }
        }
    }



    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    /**
     * 将数据发送给kafka.
     * @param data 数据
     */
    private void sendToKafka(List<String[]> data){
        try {
            data.forEach((d) -> {
                String rowKey = d[3];
                StringBuilder values = new StringBuilder();
                // TODO 考虑rowkey
                for(String s : d ) {
                    s = StringUtils.isEmpty(s)?" ":s; // 如果字段为空，则用空格代替，防止spout处理时出错
                    values.append(s).append(DELIMITER_FIELDS);
                }
//                String value = values.toString();
//                value = value.substring(0,(value.length()-1)); // 将最后一个#截取
                // 把data中的数据按照#分割，发送给kafka
                logger.info(" send qcErr to kafka!length: " + d.length);
                System.out.println(" send qcErr to kafka! length: " + d.length);
                System.out.println(values.toString());
                // 保证发送的数据根据#截取之后的长度是16
                logger.info("massage: " + values.toString());
                producer.sendMsg(rowKey + Constant.DELIMITER_FIELDS + QUALITY_MARK, values.toString());
            });
        } catch (Exception e) {
            logger.error("send data to kafka error!", e);
        }
    }
}
