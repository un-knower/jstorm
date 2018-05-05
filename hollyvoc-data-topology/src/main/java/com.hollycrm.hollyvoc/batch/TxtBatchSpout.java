package com.hollycrm.hollyvoc.batch;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.FailedException;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.TupleHelpers;
import com.alibaba.jstorm.batch.BatchId;
import com.alibaba.jstorm.batch.IBatchSpout;
import com.hollycrm.kafka.consumer.DataConsumer;
import com.hollycrm.util.config.ConfigUtils;
import com.hollycrm.hollyvoc.constant.Constant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shade.storm.org.apache.commons.lang.StringUtils;

import java.util.*;

import static com.hollycrm.hollyvoc.constant.TopoConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/7/6.
 * 获取文本并处理数据
 */
public class TxtBatchSpout implements IBatchSpout {

    public static final String NAME = "txt-batch-spout";
    private static final Logger logger = LoggerFactory.getLogger(TxtBatchSpout.class);
    private DataConsumer consumer;
    private Map<Integer,String> filedHBaseMap;
    //初始化
    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        Properties props = ConfigUtils.getProp("/consumer.properties");
        Long interval = ConfigUtils.getLongVal("poll.interval.mills", 1000L);
//        String group = (String)stormConf.get(MSG_GROUP);
        String group = "jstorm-batch-topology";
        this.consumer = new DataConsumer(BATCH_TOPIC, group == null ? TXT_MSG_GROUP_TOPOLOGY : group,
                props, interval);
        Map<String,Integer>  map = Constant.getHBaseMapping();
        filedHBaseMap = new HashMap<>(map.size());
        map.forEach((k, v) -> {
            filedHBaseMap.put(v, k);
        });
    }

    // 完成自己的逻辑，即每一次取消息后，用collector 将消息emit出去
    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {
        if(TupleHelpers.isTickTuple(input)) return;
        BatchId batchId = (BatchId) input.getValue(0);
        logger.info("txt-batch-spout batch is " + batchId.getId());
//        System.out.println("txt-batch-spout batch is " + batchId.getId());
        this.consumer.pollAndProcessMsg(crs -> {
            Iterator<ConsumerRecord<String, String>> iterator = crs.iterator();
            System.out.println("txt-batch-spout batch is " + batchId.getId() + " count " + crs.count());
           logger.info("txt-batch-spout batch is " + batchId.getId() + " count " + crs.count());
            while (iterator.hasNext()){
                // 获取消息
                ConsumerRecord<String, String> record = iterator.next();
                String rowKey = record.key(); // 文档的rowkey
                String value = record.value(); // 消息内容，除了rowkey之外的内容
//                logger.info("value:" + value);
                logger.info("rowkey" + rowKey +" record:" +record.offset());

                String[] infos = value.split("\\" + DELIMITER_PIPE);
                String userTxt="",agentTxt="",allTxt="";
                StringBuilder builder = new StringBuilder();
//                System.out.println("infos" + Arrays.toString(infos));
//                System.out.println("infos.length"+infos.length);
                for(int i=0;i<infos.length;i++){
                    // 找到文本的值添
                    String val = infos[i];
                    String file = filedHBaseMap.get(i);
                    if(userContent.equals(file)){
//                        System.out.println(userContent + i);
                        userTxt = val;
                        continue;
                    }
                    if(agentContent.equals(file)){
                        agentTxt = val;
//                        System.out.println(agentContent + i);
                        continue;
                    }
                    if(allContent.equals(file)){
//                        System.out.println(allContent + i);
                        allTxt = val;
                        continue;
                    }
                    builder.append(val).append(DELIMITER_PIPE); // 个字段之间用“|”拼接
                }
                String basicInfo = builder.substring(0,builder.length()-1);

                try {
                    collector.emit(
                            TOPOLOGY_STREAM_TXT_BACTH_ID,
                            new Values(
                                    batchId,
                                    rowKey, // rowkey
                                    rowKey.substring(10, 12), // 省份
                                    StringUtils.reverse(rowKey.substring(0, 10))
                                            .substring(0, 8), // 日期
                                    basicInfo, // info:contact
                                    userTxt, // 客户文本
                                    agentTxt, // 坐席
                                    allTxt // 全部通话内容
                            ));

                }catch (Exception e){
                    logger.error("txt-batch-spout has error", e);
                }
            }
            return true;
        });
    }

    @Override
    public void cleanup() {
        consumer.close();
    }

    // 定义spout发送数据，每个字段的含义
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(
                TOPOLOGY_STREAM_TXT_BACTH_ID,
                new Fields(DEC_BATCH_ID, DEC_ROW_KEY, DEC_PROVINCE, DEC_DAY,
                        DEC_BASIC_INFO, DEC_USER_TXT, DEC_AGENT_TXT, DEC_ALL_TXT)
        );
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    @Override
    public byte[] commit(BatchId id) throws FailedException {
        logger.debug("txt-batch-spout commit " + id.getId());
        System.out.println("txt-batch-spout commit " + id.getId());
        return null;
    }

    @Override
    public void revert(BatchId id, byte[] commitResult) {
        logger.debug("txt-batch-spout revert " + id.getId());
    }
}
