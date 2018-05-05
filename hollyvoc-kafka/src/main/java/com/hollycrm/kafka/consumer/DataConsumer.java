package com.hollycrm.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

/**
 * Created by qianxm on 2017/7/3.
 * 消费者，订阅消息，从kafka取数据
 */
public class DataConsumer {

    private static Logger logger = LoggerFactory.getLogger(DataConsumer.class);
    private long pollInterval;
    private KafkaConsumer<String, String> consumer; // 消费者对象
    public DataConsumer(String topic, String group, Properties prop, Long pollInterval) {
        this.pollInterval = pollInterval; // 间隔时间，取数据的超时时间

        //消费者可以有分组或者是单个的消费者，如果代码中设置组，则代码中组优先
        if(!(group == null || "".equals(group))){
            prop.put("group.id", group);
        }
        this.consumer = new KafkaConsumer<>(prop);
        this.consumer.subscribe(Collections.singletonList(topic));
        this.consumer.metrics();
    }

    /**
     * 接受处理消息
     * @param handler 消息处理类
     */
    public void pollAndProcessMsg(MsgHandler handler){
        logger.debug("AutoConsumer - startAccept: 接受消息");
        ConsumerRecords<String, String> crs = this.consumer.poll(pollInterval);
        if(crs.isEmpty()) {
//            logger.info("data isEmpty!");
//            System.out.println("data isEmpty!");
            return;
        }
//        logger.info(" record count: " + crs.count());
        if(handler.process(crs))
            this.consumer.commitSync();
    }

//    public void pollData(){
//        System.out.println("Consumer - startAccept: 接受消息");
//        ConsumerRecords<String, String> crs = this.consumer.poll(pollInterval);
//        if(crs.isEmpty()) {
//            System.out.println("data isEmpty!");
//            return;
//        }
//        Iterator<ConsumerRecord<String, String>> iterator = crs.iterator();
//        System.out.println("crs" + crs.count());
//        while (iterator.hasNext()){
//            // 获取消息
//            ConsumerRecord<String, String> record = iterator.next();
//            String rowKey = record.key(); // 文档的rowkey
//            String value = record.value(); // 消息内容，除了rowkey之外的内容
////            System.out.println("offset " + record.offset());
////            System.out.println("rowkey : " + rowKey);
////            System.out.println("value :" + value);
//
//        }
//    }

    public void close() {
        this.consumer.close();
    }


    /**
     * 消息处理类
     */
    public interface MsgHandler {
        boolean process(ConsumerRecords<String, String> crs);
    }




}
