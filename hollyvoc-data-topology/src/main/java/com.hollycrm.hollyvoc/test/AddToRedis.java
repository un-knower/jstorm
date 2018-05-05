package com.hollycrm.hollyvoc.test;

import com.hollyvoc.helper.redis.RedisHelper;

import java.util.HashMap;
import java.util.Map;

import static com.hollycrm.hollyvoc.constant.TopoConstant.REDIS_QC_ITEM;
import static com.hollycrm.hollyvoc.constant.TopoConstant.REDIS_QC_VESION;

/**
 * Created by qianxm on 2017/8/11.
 */
public class AddToRedis {

    /**
     * 向redis
     * @param redisKey redis的key值
     * @param file map中的key
     * @param val map 中的value
     */
    public static void add(String redisKey, String file, String val){
        RedisHelper.getInstance().add2Hash(redisKey, file, val);
    }

    public static Map<String,String> get(String redisKey){
        return RedisHelper.getInstance().getHashByKey(redisKey);
    }

    public static void main(String[] args) {
        String redisKey = REDIS_QC_ITEM;
        Map<String, String> qcItems = new HashMap<>();
//        qcItems.put("15011386274981","流量争议#1#1#漫游 OR 停机 OR 流量#(漫游|停机|流量)#beijing1#11#02,03#11,04#02#01,02# #01,03#2# ");
//        qcItems.put("15018287317512","河北质检#1#1#不满意 OR 坏人#(不满意|坏人)#admin#11,17,18# # #03# # #04#1# ");
//        qcItems.put("15018287317513","河北质检#1#1#不满意 OR 坏人#(不满意|坏人)#admin#11,17,18# # # # # # # # # ");
//        qcItems.put("15011386274983","流量争议#1#1#漫游 OR 停机 OR 流量#(漫游|停机|流量)#beijing1# # # # # # # # # ");

        qcItems.put("15058009972594","流式处理质检测试#1#1#( 工信部 OR 通管局 OR 工商局 OR 报社 OR 曝光 OR 12315 OR 消委会 OR 消费委员会 ) NOT ( 人脸识别 OR 实名 OR 绿通 OR 加急 OR 优先处理 )# #qianxmZB#18# # # # # # # # ");

        // 上传
        qcItems.forEach((k,v)->{
            add(redisKey,k,v);
        });

        String key = REDIS_QC_VESION;
        Map<String, String> version = new HashMap<>();
        version.put("15058009972594", "1");
//        version.put("15018287317512","0");
//        version.forEach((k,v)->{
//            add(key,k,v);
//        });

        System.out.println(" redisKey: " + redisKey);
        // 读取文件
        Map<String, String> qc = get(redisKey);
        System.out.println(" qc.size " + qc.size());
        qc.forEach((k,v)->{
            System.out.println("key " + k);
            System.out.println("value " + v);
        });

//        System.out.println("==========version===========");
//        Map<String, String> qcVersion = get(key); // 如果key不存在返回的是空的map.size=0
//        System.out.println(qcVersion.size());
//        qcVersion.forEach((k,v)->{
//            System.out.println("key " + k);
//            System.out.println("value " + v);
//        });

        // 如果key相同，那么是更新key 对应的value



//        boolean flag = true;
//        for(int i =0; i<10;i++){
//            System.out.println("i " + i);
//            for(int j =0;j<3;j++){
//                System.out.println("j " +j);
//                switch (j){
//                    case 0:
//                        System.out.println("i " + i + " j " + j);
//                        break;
//                    case 1:
//                        System.out.println("i " + i + " j " + j);
//                        break;
//                }
//                if(i==5 && j==2){
//                    flag = false;
//                    System.out.println(" 继续下一项 ！ ");
//                    break;
//                }
//                System.out.println("符合条件的 i" + i);
//            }
//
//        }


//        Map<Integer,Integer> map1 = new HashMap<>();
//        Map<Integer,Integer> map2 = new HashMap<>();
//        for(int i=0;i<10;i++){
//            map1.put(i,i);
//            map2.put(i,i);
//        }

//        for (map1)
//
//        map1.forEach((k,v)->{
//            System.out.println("i " + k);
//            map2.forEach((k1,v1)->{
//                switch (k1){
//                    case 2:
//                        System.out.println("i " + k + " j " + k1);
//                        break;
//                    case 3:
//                        System.out.println("i " + k + " j " + k1);
//                        break;
//                }
//                if(k1==4){
//                    return;
//                }
//            });
//        });


        //================读取新词
//        Map<String,String> nws = get(REDIS_NW_KEY);
//        System.out.println(nws.size());
//        StringBuilder builder = new StringBuilder();
//        nws.forEach((k,v)->{
//            System.out.println(k +" " + v);
//            builder.append(k).append("#").append(v).append("\n");
//        });
//        try {
//            String output = "D:\\myWork\\Jstorm\\data\\redis\\newword";
//            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output), "utf-8"));
//            out.println(builder.toString());
//            out.close();
//        } catch (Exception e) {
//
//        }
//        ConcurrentHashMap<String, Integer> index = new ConcurrentHashMap<String, Integer>();
//        for(int j=0;j<10;j++) {
//
//            index.computeIfAbsent("18", i -> 0);
//            int i = index.get("18");
//            i++;
//            index.put("18", i);
//        }
//        index.forEach((k, v) -> {
//            System.out.println("k: " + k + " v: " + v);
//        });
//
//        Map<String,String> nws = get("topic-offset");
//        System.out.println(nws.size());
//        StringBuilder builder = new StringBuilder();
//        nws.forEach((k,v)->{
//            System.out.println(k +" " + v);
////            builder.append(k).append("#").append(v).append("\n");
//        });
//        Map<String, Long> map = new HashMap<>();
//        map.put("1",1L);
//
//        System.out.println(map.get("2"));
    }
}
