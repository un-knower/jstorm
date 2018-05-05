package com.hollycrm.hollyvoc.qc;

import com.hollyvoc.helper.redis.RedisHelper;

import java.util.HashMap;
import java.util.Map;

import static com.hollycrm.hollyvoc.qc.QCConstant.REDIS_QC_ITEM;
import static com.hollycrm.hollyvoc.qc.QCConstant.REDIS_QC_VESION;

/**
 * Created by qianxm on 2017/9/19.
 */
public class PutQcitemToRedis {
    /**
     * 向redis
     * @param redisKey redis的key值
     * @param file map中的key
     * @param val map 中的value
     */
    private static void add(String redisKey, String file, String val){
        RedisHelper.getInstance().add2Hash(redisKey, file, val);
    }

    private static Map<String,String> get(String redisKey){
        return RedisHelper.getInstance().getHashByKey(redisKey);
    }

    public static  void putItem(String redisKey){
//        String redisKey = REDIS_QC_ITEM;
        // TODO 数据库查询获取
        Map<String, String> qcItems = new HashMap<>();
//        1	15058009972594	1	流式处理质检测试	( 工信部 OR 通管局 OR 工商局 OR 报社 OR 曝光 OR 12315 OR 消委会 OR 消费委员会 ) NOT ( 人脸识别 OR 实名 OR 绿通 OR 加急 OR 优先处理 )	1	1	sys	1	qianxmZB	2017-09-19 14:03:17	qianxmZB	2017-09-19 14:03:17	18			05
        // qcId\name\ 质检项名称|质检类型|角色|文本内容|正则内容|创建人|省份|cust_band|cust_level|satisfication|business_type|direction|recoinfoLengith |silenceLength|agentCode|
//        qcItems.put("15011386274981","流量争议#1#1#漫游 OR 停机 OR 流量#(漫游|停机|流量)#beijing1#11#02,03#11,04#02#01,02# #01,03#2# ");
        qcItems.put("15018287317512","河北质检#1#1#不满意 OR 坏人#(不满意|坏人)#qianxmZB#17,18# # #03# # # # # ");
//        qcItems.put("15018287317513","河北质检#1#1#不满意 OR 坏人#(不满意|坏人)#admin#11,17,18# # # # # # # # # ");
//        qcItems.put("15011386274983","流量争议#1#1#漫游 OR 停机 OR 流量#(漫游|停机|流量)#beijing1# # # # # # # # # ");
            qcItems.put("15058009972594","流式处理质检测试#1#1#( 工信部 OR 通管局 OR 工商局 OR 报社 OR 曝光 OR 12315 OR 消委会 OR 消费委员会 ) NOT ( 人脸识别 OR 实名 OR 绿通 OR 加急 OR 优先处理 )# #qianxmZB#18#05# # # # # # # ");
         //上传
//        qcItems.forEach((k,v)->{
//            add(redisKey,k,v);
//        });

        String key = REDIS_QC_VESION;
        Map<String, String> version = new HashMap<>();
        version.put("15058009972594", "0");
//        version.forEach((k,v)->{
//            add(key,k,v);
//        });

        Map<String, String> qc = get(redisKey);
        qc.forEach((k,v)->{
            System.out.println("key " + k);
            System.out.println("value " + v);
        });

    }

    public static void main(String[] args) {
        putItem(REDIS_QC_ITEM);
    }
}
