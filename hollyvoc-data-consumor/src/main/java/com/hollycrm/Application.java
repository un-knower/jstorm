package com.hollycrm;


import com.hollycrm.util.config.ConfigUtils;

/**
 * Created by qianxm on 2017/7/9.
 *
 */
public class Application {

    public static void main(String[] args) throws Exception{
        System.out.println("ooo" + ConfigUtils.class );
//        System.out.println(" " + ConfigUtils.class.getClassLoader().getResource("consumer.properties"));
//        System.out.println(" " + ConfigUtils.class.getClassLoader().getResource("application.properties"));
        String topic = "basic-topic";
        topic = "json-topic";
        SimpleConsumer consumer = new SimpleConsumer(topic, ConfigUtils.getIntVal("worker.thread", 2));
        consumer.start();
        while (true) {
            try {
                Thread.sleep(60000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
