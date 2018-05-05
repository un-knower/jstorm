package com.hollycrm.hollyvoc.error;

import com.hollycrm.kafka.consumer.DataConsumer;
import com.hollycrm.kafka.producer.DataProducer;
import com.hollycrm.util.config.ConfigUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by qianxm on 2017/8/21.
 * 异常数据处理，将error-topic 中的数据发送给正常数据处理流程的txt-topic。
 */
public class ErrorDataTreat {
    private static Logger log = LoggerFactory.getLogger(ErrorDataTreat.class);

    private Thread[] workers;

    private volatile static Set<String> executed = ConcurrentHashMap.newKeySet();

    private DataProducer producer;

    public ErrorDataTreat(String consTopic,String proTopic, int workerNum) {
        System.out.println(" init SimpleConsumer ");
        Properties prop = ConfigUtils.getProp("/consumer.properties");
        Properties producerPro = ConfigUtils.getProp("/producer.properties");
        String gropId = "error-topic-test";
//        String gropId = "txt-data";

        producer = new DataProducer(proTopic, producerPro);
        workers = new Thread[workerNum];
        for(int i = 0; i < workerNum; i++) {
            workers[i] = new Thread(()->{
                System.out.println("init consumer");
                System.out.println(prop.toString());
                System.out.println("topic " + consTopic + " group " + gropId);
                DataConsumer consumer = new DataConsumer(consTopic, gropId, prop, 2000L);
//                System.out.println("get data");
                while (true){
                    consumer.pollAndProcessMsg(crs -> {
                        System.out.println("==========crs===========" + crs.count());
                        Iterator<ConsumerRecord<String, String>> iterator = crs.iterator();
                        while (iterator.hasNext()){
                            // 获取消息
                            ConsumerRecord<String, String> record = iterator.next();
                            String rowKey = record.key(); // 文档的rowkey
                            String value = record.value(); // 消息内容，除了rowkey之外的内容
                            producer.sendMsg(rowKey, value);
                            System.out.println("send rowkey : " + rowKey );
//                            System.out.println("send value :" + value);

                        }
                        return true;
                    });
                }
            }, "Consumer-Worker-" + i);
        }

    }


    public void start() {
        for(Thread worker : workers) {
            worker.start();
            System.out.println(worker.getName() + " is started!");
            log.info(worker.getName() + " is started!");
        }
    }
}
