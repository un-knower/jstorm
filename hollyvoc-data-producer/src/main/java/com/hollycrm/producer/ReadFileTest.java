package com.hollycrm.producer;

import com.hollycrm.kafka.producer.DataProducer;
import com.hollycrm.util.config.ConfigUtils;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hollycrm.hollyvoc.constant.Constant.DELIMITER_PIPE;


/**
 * Created by qianxm on 2017/8/26.
 *
 * 重复数据不重复上传kafka处理，即将文件中的rowkey 做自加操作。
 */

public class ReadFileTest {
    private BlockingQueue<String> queue = new ArrayBlockingQueue<String>(1000);
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ReadFileTest.class);
    private Thread reader;
    private Thread[] workers;
    private AtomicInteger counter = new AtomicInteger(0);
    private volatile static Set<String> executed = ConcurrentHashMap.newKeySet();
//    String topic = "basic-topic";
    String topic = "index-thread-topic";
    public ReadFileTest(String path,int workerNum){
        reader = new Thread(() -> {
            System.out.println(path);
            File pathFile = new File(path);
            int i=0;
                while (true) {
                    File[] files = pathFile.listFiles();
                    if(files != null  && files.length > 0) {
                        for (File file : files) {
                            if (executed.contains(file.getName())) continue;
//                    File file = new File(fileName);
                            System.out.println("read file :" + file.getName());
                            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))) {
                                String line;
                                while ((line = br.readLine()) != null) {
                                    int first = line.indexOf(DELIMITER_PIPE);
                                    String rowKey = line.substring(0, first);  // 第一个|前面的是rowkey
                                    String info = line.substring(first + 1);
//                                    System.out.println(rowKey + i++);
                                    String row = rowKey + i++;
//                            System.out.println(row+info);
                                    queue.put(row + DELIMITER_PIPE + info);
                                }
                            } catch (Exception e) {
                                log.error("Producer-Reader", e);
                            }
                        }
                        System.out.println("queue.size"+queue.size());

                        try { Thread.sleep(3000L); }catch (Exception e) {e.printStackTrace();}
                    }
                    System.out.println("queue.size"+queue.size());
                    try { Thread.sleep(3000L); }catch (Exception e) {e.printStackTrace();}

                }
        },"read");


        Properties prop = ConfigUtils.getProp("/producer.properties");

        workers = new Thread[workerNum];
        for(int i = 0; i < workerNum; i++) {
            workers[i] = new Thread(()->{
                // kafka producer
                DataProducer producer = new DataProducer(topic, prop);

                while (true) {
                    try {
                        String line = queue.take();
                        int first = line.indexOf(DELIMITER_PIPE);
                        String rowKey = line.substring(0, first);  // 第一个|前面的是rowkey
                        producer.sendMsg(rowKey,line.substring(first + 1));
                        System.out.println(Thread.currentThread().getName() + " send:" + rowKey +  "  counter=" + counter.incrementAndGet());
                    }catch (InterruptedException e) {
                        log.error(Thread.currentThread().getName(), e);
                    }
                }
            }, "Producer-Worker-" + i);
        }
    }
    private void start(){
        reader.start();
        for(Thread worker : workers) {
            worker.start();
            System.out.println(worker.getName() + " is started!");
        }
    }

    public static void main(String[] args) {
        String file = "D:\\cluster\\test\\";
//        String file= "D:\\cluster\\74-0503";
        file = "D:\\myWork\\data\\";
//        file = "D:\\myWork\\data\\21\\";
        ReadFileTest readft = new ReadFileTest(file,3);
//        readft.readFile("D:\\cluster\\test.txt");
        readft.start();
    }

}
