package com.hollycrm.hollyvoc.qc.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.hollycrm.hollyvoc.qc.regular.TxtMatch;
import com.hollycrm.hollyvoc.constant.Constant;
import com.hollyvoc.helper.redis.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shade.storm.org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.hollycrm.hollyvoc.qc.QCConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/7/6.
 * 质检：首先需要查询质检项，获取redis中的质检项条件，然后在根据质检项条件对录音文本进行匹配
 */
public class QualityBolt implements IRichBolt {
    public static final String NAME = "quality-bolt";

    private static Logger logger = LoggerFactory.getLogger(QualityBolt.class);
    private static Map<String, Integer> txtFiledMap; // 录音信息字段-索引位置

    private static Map<String, Integer> qcItmeFiledMap; // 质检字段-索引位置
    private static Map<String, Integer> qcFiledMap; // 需要质检的字段

    private ConcurrentHashMap<String, String> qcItems = new ConcurrentHashMap<>(); // redis 中的质检项 key-质检id、value 存放质检项
    private ConcurrentHashMap<String, String> qcVesions = new ConcurrentHashMap<>(); // redis版本，用来更新缓存
    private ConcurrentHashMap<String, Long> flushTimer = new ConcurrentHashMap<>(); // 刷新缓存的计时器
    private OutputCollector collector;

    private TxtMatch txtMatch;
    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        try {
            this.collector = outputCollector;
            txtFiledMap = Constant.getHBaseMapping(); // 文本记录字段
            qcItmeFiledMap = Constant.getQcMapping(); // 质检项字段
            // 需要进行质检的字段
            qcFiledMap= Constant.getQcMapping();

            // 移除不是质检条件的字段
            qcFiledMap.remove(qcName);
            qcFiledMap.remove(qcType);
            qcFiledMap.remove(qcCreator);
            qcFiledMap.remove(qcTxtReg);
            // TODO 读取redis质检数据,暂时没有数据先本地创建数据测试流程
            // 刷新本地缓存时用redis获取到的数据与本地对比，如果不同修改，如果为空则是新的数据，需要添加
            qcItems.putAll(RedisHelper.getInstance().getHashByKey(REDIS_QC_ITEM));
            qcVesions.putAll(RedisHelper.getInstance().getHashByKey(REDIS_QC_VESION));
            flushTimer.put(TIME_KEY, System.currentTimeMillis());
            txtMatch = TxtMatch.getInstance();
            // 启动刷新本地缓存线程
        } catch (Exception e){
            throw new RuntimeException(" init redis error!", e);
        }
    }

    @Override
    public void execute(Tuple tuple) {

        try {

            if (TOPOLOGY_STREAM_QC_ID.equals(tuple.getSourceStreamId())) {
                System.out.println("------------ qc-bolt -------------");
                // 满足条件需要保存质检的数据
                // 接受上一级bolt发送来的数据
                String rowKey = tuple.getStringByField(DEC_ROW_KEY);
                logger.info(" qc rowkey: " + rowKey);
                String basicInfo = tuple.getStringByField(DEC_BASIC_INFO);
                String agentTxt = tuple.getStringByField(DEC_AGENT_TXT);
                String userTxt = tuple.getStringByField(DEC_USER_TXT);
                String allTxt = tuple.getStringByField(DEC_ALL_TXT);
                String prov = tuple.getStringByField(DEC_PROVINCE);
                String[] basicInfos = basicInfo.split("\\" + DELIMITER_PIPE);
                // 对数据进行质检,轮训质检项
                // 对数据进行质检,轮训质检项
                qcItems.forEach((k, v) -> {

                    // 进行质检，根据fieldName 进行
                    // 1. 将质检项中的条件截取,各个质检字段按#分割.
//                    System.out.println(" qcField: " + v);
                    String[] qcFields = v.split(DELIMITER_FIELDS); // 质检条件
//                    System.out.println( " qcFiledMap length " + qcFiledMap.size() + " qcFields length " + qcFields.length);
                    String words = "", role = "";
                    // 2. 获取质检字段的值进行质检，轮询质检条件
                    Set<String> keys = qcFiledMap.keySet();
//                    boolean isMactch = true;
                    // todo 质检类型、名称、创建人不需要匹配。
                    for (String field : keys) {
                        String value = qcFields[qcFiledMap.get(field)]; // 质检项中质检字段的值
                        // 如果没有值继续下一个质检条件
                        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(value.replaceAll(" ", ""))) {
                            continue;
                        }
                        if (qcTxtReg.equals(field) || qcTxtContent.equals(field)) { // 不许要验证
                            continue;
                        }

                        // 根据field 获取录音录音记录对应的值
                        // 文本匹配比较特殊
                        String textContent = allTxt; // 需要匹配的文本
//                            String r = "";
                        // 如果是角色字段，则需要进行文本匹配
                        if (qcRole.equals(field)) {
                            if (!StringUtils.isEmpty(value)) {
                                role = value;
                                // 根据角色选择不同的文本进行匹配
                                switch (value) {
                                    case allRole:
                                        textContent = allTxt;
                                        break;
                                    case userRole:
                                        textContent = userTxt;
                                        break;
                                    case agentRole:
                                        textContent = agentTxt;
                                        break;
                                }
                            }
                            // todo 如果角色不为空则进行文本匹配
                            // 文本内容不为空 进行内容匹配
                            if (StringUtils.isNotEmpty(textContent)) {
                                // 获取质检项正则表达式 对文本进行匹配
                                String reg = qcFields[qcFiledMap.get(qcTxtContent)];
                                words = txtMatch.getMatchWords(textContent, reg);
                                // 匹配不到结果即不符合质检条件
                                if (StringUtils.isEmpty(words)) {
//                                    isMactch = false;
                                    break;
                                }
                            }
                            // 角色有值 进行文本匹配
                            continue;
                        }

                        // file中有几个字段txtFile中没有 qcType、qcName、角色、creator
                        // 如果不是录音内容匹配，则匹配的是结果化维度
                        int index = 0;
                        if (txtFiledMap.containsKey(field)) {
                            index = txtFiledMap.get(field);

                        }

                        // 省份字段名称是areaCode，并非是province
                        String txtValue = basicInfos[index]; // 对应的字段值
                        if (!StringUtils.isEmpty(txtValue)) {
                            if (!value.contains(txtValue)) { // 如果包含则满足条件
//                                    isMactch = false;
                                // rerurn 外不循环也会跳出，break 跳出单个循环
                                break; // 如果不满足条件，就对下一个质检项的条件进行匹配
                            }
                        }
                    }

                    // 质检项中的文本内容不为空，所以只有匹配到数据的记录符合质检条件需要保存到数据库
                    if(StringUtils.isEmpty(words)){
                        // foreach中的return 相当于continue
                        return;
                    }

                    // 全部通过的就是符合条件的，需要发送到下游，保存到数据库
                    String qcName = qcFields[qcItmeFiledMap.get(Constant.qcName)];
                    String qcCreator = qcFields[qcItmeFiledMap.get(Constant.qcCreator)];
                    String qcType = qcFields[qcItmeFiledMap.get(Constant.qcType)];

                    // 匹配到的数据才会发送
                    collector.emit(TOPOLOGY_ORACLE_ID, new Values(k, qcName,
                        rowKey, qcType, qcCreator, role, qcFields[qcFiledMap.get(qcTxtContent)], words, prov,
                        basicInfos[txtFiledMap.get(custBrand)], basicInfos[txtFiledMap.get(satisfication)],
                        basicInfos[txtFiledMap.get(businessType)], basicInfos[txtFiledMap.get(silenceLength)],
                        basicInfos[txtFiledMap.get(userCode)],basicInfos[txtFiledMap.get(acceptTime)],
                        basicInfos[txtFiledMap.get(caller)], basicInfos[txtFiledMap.get(callee)]));
                });
                collector.ack(tuple);
            }
        } catch (Exception e) {
            // 匹配时的异常情况
            logger.error(" qc err!", e);
            collector.fail(tuple);
        }

        // todo 如果刷新失败，对于更新的质检不起做用,是否需要将数据发送给kafka？如果没有更新则不影响，如果存在更新则会影响结果。。
        try {
            //  刷新本地缓存
            flushQcItem();
            logger.info("定时刷新QCItem缓存，质检项数量：" + qcItems.size());
        } catch (Exception e1) {
            // 异常可能是链接不到redis
            logger.error(" redis error!", e1);
        }

    }



    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream(TOPOLOGY_ORACLE_ID, new Fields(
                qcId, qcName, id, qcType, qcCreator, qcRole, qcTxtContent, qcMatchWord,
                province, custBrand, satisfication, businessType,
                silenceLength, userCode,acceptTime,caller,callee));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    /**
     * 定时刷新质检项缓存，每两个小时刷新一次
     */
    private void flushQcItem() {

        if((System.currentTimeMillis() - flushTimer.get(TIME_KEY))/1000 > 7200){
            Map<String, String> items = RedisHelper.getInstance().getHashByKey(REDIS_QC_ITEM); // redis 中的质检项 key-质检id、value 存放质检项
            Map<String, String> version =RedisHelper.getInstance().getHashByKey(REDIS_QC_VESION);
            version.forEach((k,v)->{
                // 如果本地的版本号获取为空值，说明条记录是新添加的
                if(StringUtils.isEmpty(qcVesions.get(k))){
                    qcItems.put(k,items.get(k));
                } else if(!v.equals(qcVesions.get(k))){ // 如果本地的版本与最新的版本不一致，更新id对应的值
                    qcItems.put(k,items.get(k));
                }
            });
            flushTimer.put(TIME_KEY,System.currentTimeMillis());
        }

    }
}

