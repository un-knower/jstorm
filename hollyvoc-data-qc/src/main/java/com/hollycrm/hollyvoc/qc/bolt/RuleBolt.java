package com.hollycrm.hollyvoc.qc.bolt;

import backtype.storm.command.list;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.hollycrm.hollyvoc.constant.ConstUtils;
import com.hollycrm.hollyvoc.constant.Constant;
import com.hollycrm.hollyvoc.qc.bean.CustcontentInfo;
import com.hollycrm.hollyvoc.qc.rule.Rule;
import com.hollycrm.hollyvoc.qc.rule.RuleContext;
import com.hollycrm.hollyvoc.qc.rule.RuleRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hollycrm.hollyvoc.constant.Constant.*;
import static com.hollycrm.hollyvoc.qc.QCConstant.*;

/**
 * Created by qianxm on 2017/12/4.
 */
public class RuleBolt implements IRichBolt{
    public static final String NAME = "rule-bolt";

    private static Logger logger = LoggerFactory.getLogger(RuleBolt.class);
    private static Map<String, Integer> txtFiledMap; // 录音信息字段-索引位置
    private OutputCollector collector;
    private Integer sheetNoIdx;
    private Map<String, String> qcItem;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        txtFiledMap = Constant.getHBaseMapping(); // 文本记录字段
        txtFiledMap.remove(userContent);
        txtFiledMap.remove(agentContent);
        txtFiledMap.remove(allContent);

        collector = outputCollector;
        sheetNoIdx = txtFiledMap.get(sheetNo);

        // 规则模板: for (int i = 0; i < CustcontentInfo.size(); i++) {sr = CustcontentInfo.get(i);if (mobel.contains(sr.getMobileNo())) {result.add(sr);}}for (int i = 0; i < CustcontentInfo.size(); i++) {sr = CustcontentInfo.get(i);if (mobel.contains(sr.getMobileNo())) {result.add(sr);}}
        // 质检项
        qcItem = new HashMap<>();
        // key 质检id, 质检规则类型，质检规则，质检参数
        qcItem.put("qc201712005152523","01#电话号码#if (params.contains(CustcontentInfo.getMobileNo())) {result.add(sr);}#17633169418 17603388501 17633169418 15358706933 17633205976 17603296353");
    }

    @Override
    public void execute(Tuple tuple) {
        try {

            if (TOPOLOGY_STREAM_QC_ID.equals(tuple.getSourceStreamId())) {
                System.out.println("------------ qc-bolt -------------");
                // 满足条件需要保存质检的数据
                // 接受上一级bolt发送来的数据
                String rowKey = tuple.getStringByField(DEC_ROW_KEY);
//                logger.info(" qc rowkey: " + rowKey);
//                System.out.println(" qc rowkey: " + rowKey);
                String basicInfo = tuple.getStringByField(DEC_BASIC_INFO);
                String agentTxt = tuple.getStringByField(DEC_AGENT_TXT);
                String userTxt = tuple.getStringByField(DEC_USER_TXT);
                String allTxt = tuple.getStringByField(DEC_ALL_TXT);
                String prov = tuple.getStringByField(DEC_PROVINCE);
                String[] basicInfos = basicInfo.split("\\" + DELIMITER_PIPE);
                long duration =  Long.parseLong(basicInfos[txtFiledMap.get(recordLength)]);
                long silence = Long.parseLong(basicInfos[txtFiledMap.get(silenceLength)]);
//                doc.setField(recordLengthRange, ConstUtils.getRecoinfoLengthRangeCode(duration));
//                doc.setField(silenceLengthRange, ConstUtils.getSilenceRangeCode(duration, silence));

                String sheet ;
                if(sheetNoIdx == null){
                    sheet = null;
                }else {
                    sheet = basicInfos[sheetNoIdx];
                }
                // 判断是否有工单
                String hasSheet = sheet == null || "".equals(sheet) || "null".equals(sheet) ? "0" : "1";
                // 将数据截取放到对象中
                CustcontentInfo custs = new CustcontentInfo(basicInfos[txtFiledMap.get(custinfoId)],
                        basicInfos[txtFiledMap.get(areaCode)],basicInfos[txtFiledMap.get(userCode)],
                        basicInfos[txtFiledMap.get(callee)], basicInfos[txtFiledMap.get(caller)],
                        basicInfos[txtFiledMap.get(mobileNo)], basicInfos[txtFiledMap.get(acceptTime)],
                        basicInfos[txtFiledMap.get(year)], basicInfos[txtFiledMap.get(month)],
                        basicInfos[txtFiledMap.get(day)],basicInfos[txtFiledMap.get(week)],
                        basicInfos[txtFiledMap.get(custArea)], basicInfos[txtFiledMap.get(custBrand)],
                        basicInfos[txtFiledMap.get(satisfication)], basicInfos[txtFiledMap.get(queue)],
                        basicInfos[txtFiledMap.get(serviceType)], basicInfos[txtFiledMap.get(sheetNo)],
                        basicInfos[txtFiledMap.get(sheetType)],basicInfos[txtFiledMap.get(custLevel)],
                        basicInfos[txtFiledMap.get(businessType)], basicInfos[txtFiledMap.get(recordLength)],
                        basicInfos[txtFiledMap.get(silenceLength)],ConstUtils.getRecoinfoLengthRangeCode(duration),
                        ConstUtils.getSilenceRangeCode(duration, silence),
                        userContent, agentContent, allContent, basicInfos[txtFiledMap.get(direction)],
                        basicInfos[txtFiledMap.get(recordFormat)], basicInfos[txtFiledMap.get(recordSampRate)],
                        basicInfos[txtFiledMap.get(recordEncodeRate)], hasSheet,
                        basicInfos[txtFiledMap.get(netType)], basicInfos[txtFiledMap.get(hour)]);
                // 质检规则

//                System.out.println("id :" + custs.getCustinfoId() + " model:" + custs.getMobileNo());
                // todo 修改为对象
//                List<CustcontentInfo> list = new ArrayList<>();
//                list.add(custs);
                qcItem.forEach((k,v)->{
                    String qcId = k;
                    String[] qcDetail = v.split(DELIMITER_FIELDS);
                    String express = qcDetail[2];
                    //System.out.println("express:" + express);

                    List<Rule> ruleList=new ArrayList<Rule>();
//                    System.out.println("mobel" + qcDetail[3]);
//                    System.out.println("qcDetail[3].split(\" \")" + qcDetail[3].split(" ")[0]) ;
                    List<String> params = Arrays.asList(qcDetail[3].split(" "));
//                    System.out.println("params " + params.size());
//                    params.forEach(p->{
//                        System.out.println("p" + p);
//                    });
                    Rule aRule=new Rule();
                    aRule.setId(Integer.valueOf(qcDetail[0]));
                    aRule.setRuleName(qcDetail[1]);
                    aRule.setGroupType(1);
                    aRule.setGroupName(qcDetail[1]);
                    aRule.setMatchCondition("true");
                    aRule.setExecuteContent(express);
                    ruleList.add(aRule);
                    List<CustcontentInfo> result = new ArrayList<>();
                    RuleRunner ruleRunner = new RuleRunner();
                    ruleRunner.setRuleList(ruleList);
                    RuleContext<String,Object> ruleContext = new RuleContext<String,Object>();
                    ruleContext.put("CustcontentInfo", custs);
                    ruleContext.put("params", params);
                    ruleContext.put("result", result);
                    //这里设置规则执行上下文信息
                    ruleContext.setGroupType(1);
                    try {
                        //规则分组，1:库存占位
                        ruleRunner.dispatch(ruleContext);//开始解析并执行规则
                    } catch (Exception e) {
                        logger.error(" 规则质检error ：",e);
                    }

                    result.forEach(c->{
                        logger.info("匹配到：id： " + c.getCustinfoId() + " mobel: " + c.getMobileNo());
                        System.out.println(("匹配到：id： " + c.getCustinfoId() + " mobel: " + c.getMobileNo()));
                    });
                });

            }
        } catch (Exception e) {
            // 匹配时的异常情况
            logger.error(" qc err!", e);
            collector.fail(tuple);
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
