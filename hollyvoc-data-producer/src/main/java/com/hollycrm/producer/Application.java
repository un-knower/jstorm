package com.hollycrm.producer;


import com.hollycrm.util.config.ConfigUtils;

/**
 * Created by alleyz on 2017/5/16.
 *
 */
public class Application{

    public static void main(String[] args) {
        System.out.println("ooo" + ConfigUtils.class );
        System.out.println("file.path"+ConfigUtils.getStrVal("file.path"));
        String topic = "index-thread-topic";
//        topic = "qc-topic";
//        topic = "json-topic";
        topic = "data-11";
        MultiProducer producer = new MultiProducer(ConfigUtils.getStrVal("file.path"),
                topic, ConfigUtils.getIntVal("worker.thread", 2));
        producer.start();
        while (true) {
            try {
                Thread.sleep(60000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
