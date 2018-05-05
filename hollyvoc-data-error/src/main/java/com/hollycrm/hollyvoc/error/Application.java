package com.hollycrm.hollyvoc.error;


import com.hollycrm.util.config.ConfigUtils;
import lombok.extern.log4j.Log4j;

import java.util.Arrays;

/**
 * Created by qianxm on 2017/7/9.
 *
 */
@Log4j
public class Application {

    public static void main(String[] args) throws Exception{
        if(args.length < 2){
            log.error(" please input -error.topic errorTopicName -txt.topic:txtTopicName!");
//            System.err.println(" please input -error.topic:errorTopicName -txt.topic:txtTopicName!");
            System.exit(1);
        }
        System.out.println(Arrays.toString(args));
        String consTopic="", // 异常topic名称
                proTopic=""; // 正常topic名称
        for(String param: args) {
            System.out.println(" param " + param);
            if (param.contains("-error.topic")) {
                consTopic = param.split(":")[1];
                continue;
            }
            if (param.contains("-txt.topic")) {
                proTopic = param.split(":")[1];
            }

        }
        System.out.println("consTopic: " + consTopic + " proTopic : "  + proTopic) ;

        if(isEmpty(consTopic)){
            log.error(" please input  -error.topic:errorTopicName!");
            System.exit(1);
        }
        if (isEmpty(proTopic)){
            log.error(" please input -txt.topic:txtTopicName!");
            System.exit(1);
        }

        ErrorDataTreat treat = new ErrorDataTreat(consTopic,proTopic, ConfigUtils.getIntVal("worker.thread", 2));
        treat.start();
//        while (true) {
//            try {
//                Thread.sleep(60000);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
