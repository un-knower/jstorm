package com.hollycrm.producer;


import com.hollycrm.kafka.producer.DataProducer;
import com.hollycrm.util.config.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hollycrm.hollyvoc.constant.Constant.DELIMITER_PIPE;

/**
 * Created by alleyz on 2017/5/16.
 * 多线程生产消息
 *  读取指定目录下的文件,将文件内容当做消息发送给kafka
 */
public class MultiProducer {

    private static Logger log = LoggerFactory.getLogger(MultiProducer.class);

    private BlockingQueue<String> queue = new ArrayBlockingQueue<String>(1000);

    private Thread reader;

    private Thread[] workers;

    private volatile static Set<String> executed = ConcurrentHashMap.newKeySet();

    private AtomicInteger counter = new AtomicInteger(0);

    public MultiProducer(String path, String topic, int workerNum) {
        File pathFile = new File(path);
        reader = new Thread(() -> {
            while (true) {
                File[] files = pathFile.listFiles();
                if(files != null  && files.length > 0) {
                    for (File file : files) {
                        if(executed.contains(file.getName())) continue;

                        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))){
                            String line;
                            while ((line = br.readLine()) != null)
                                queue.put(line);
                            executed.add(file.getName());
                        }catch (IOException | InterruptedException  e){
                            log.error("Producer-Reader", e);
                        }
                    }
                }
                try { Thread.sleep(3000L); }catch (Exception e) {e.printStackTrace();}
            }
        }, "Producer-Reader");

        Properties prop = ConfigUtils.getProp("/producer.properties");
        workers = new Thread[workerNum];
        for(int i = 0; i < workerNum; i++) {
            workers[i] = new Thread(()->{
                DataProducer producer = new DataProducer(topic, prop);
                while (true) {
                    try {
                        String line = queue.take();
                        int first = line.indexOf(DELIMITER_PIPE);
//                        System.out.println("line" + line);
                        String rowKey = line.substring(0, first);  // 第一个|前面的是rowkey
                        producer.sendMsg(rowKey,line.substring(first + 1));
                        System.out.println(Thread.currentThread().getName() + " send:" + rowKey + " , prov: " +rowKey.substring(10,12) +  "  counter=" + counter.incrementAndGet());
                    }catch (InterruptedException e) {
                        log.error(Thread.currentThread().getName(), e);
                    }
                }
            }, "Producer-Worker-" + i);
        }

    }


    public void start() {
        reader.start();
        log.info(reader.getName() + " is started!");
        for(Thread worker : workers) {
            worker.start();
            log.info(worker.getName() + " is started!");
        }
    }
}
