package com.hollycrm.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by qianxm on 2017/7/3.
 * kafka的 producer 想broker提供消息， consumer才会接到通知处理消息。
 * kafka中消息单位是topic。
 * kafka依赖于zookeeper来协调，
 */

public class DataProducer {
    private static Logger log = LoggerFactory.getLogger(DataProducer.class);
    private String topic; // 消息
    private org.apache.kafka.clients.producer.KafkaProducer producer; // producer

    public DataProducer(String topic, Properties prop){
        log.info("build producer topic is " + topic);
        this.topic = topic;
        this.producer = new org.apache.kafka.clients.producer.KafkaProducer(prop);
    }




    /**
     * 发送消息
     * @param record {@link ProducerRecord}
     */
    private void send(ProducerRecord<String, String> record) {
        this.producer.send(record);
    }

    /**
     * 发送消息，通过k-v的形式
     * @param key key
     * @param value value
     */
    public void sendMsg(String key, String value) {
        send(new ProducerRecord<>(this.topic, key, value));
    }




}
