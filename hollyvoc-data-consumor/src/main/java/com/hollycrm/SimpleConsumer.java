package com.hollycrm;

import com.hollycrm.kafka.consumer.DataConsumer;
import com.hollycrm.util.config.ConfigUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by qianxm on 2017/7/9.
 *
 */
public class SimpleConsumer {
    private static Logger log = LoggerFactory.getLogger(SimpleConsumer.class);

    private Thread[] workers;

    private volatile static Set<String> executed = ConcurrentHashMap.newKeySet();
    private static int count=0;

    /**
     * 计数器自增.
     */
    private static synchronized void countIncrement(){
        count++;
        if(count > 9999) count = 0;
    }

    public SimpleConsumer(String topic, int workerNum) throws Exception {
        System.out.println(" init SimpleConsumer ");
        Properties prop = ConfigUtils.getProp("/consumer.properties");
        String outPut = "D:\\myWork\\Jstorm\\kafka-data\\dataFile";
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outPut), "utf-8"));

//        String gropId = "data";
        String gropId = "basic-topology";
        workers = new Thread[workerNum];

        for(int i = 0; i < workerNum; i++) {
            workers[i] = new Thread(()->{
                System.out.println("init consumer");
                System.out.println(prop.toString());
                System.out.println("topic " + topic + " group " + gropId);
               DataConsumer consumer = new DataConsumer(topic, gropId, prop, 1000L);
//                ConsumerRecord<String, String> cr= consumer.
                System.out.println("get data");
//                consumer.pollData();
                while (true){
                    StringBuilder builder = new StringBuilder();
                    consumer.pollAndProcessMsg(crs -> {
                        System.out.println("==========crs===========" + crs.count());
                        Iterator<ConsumerRecord<String, String>> iterator = crs.iterator();
                        while (iterator.hasNext()){
                            // 获取消息
                            ConsumerRecord<String, String> record = iterator.next();
                            String rowKey = record.key(); // 文档的rowkey
                            String value = record.value(); // 消息内容，除了rowkey之外的内容

//                            System.out.println("offset " + record.offset());
                            System.out.println("rowkey : " + rowKey );
//                            System.out.println("prov " + rowKey.substring(10,12));
                            System.out.println("value :" + value);
//                            out.println(rowKey+ "|" + value + "\r");
                            builder.append(rowKey).append("|").append(value).append("\r");
                        }
                        out.println(builder.toString());
                        out.flush();
                        out.close();
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
