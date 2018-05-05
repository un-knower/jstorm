package com.hollycrm.hollyvoc.qc.kryobolt;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.alibaba.jstorm.kafka.KafkaConstant;
import com.hollycrm.hollyvoc.constant.ByteUtil;
import com.hollycrm.hollyvoc.constant.ConstUtils;
import com.hollycrm.hollyvoc.constant.Constant;
import com.hollycrm.hollyvoc.qc.bolt.DataProcess;
import com.hollycrm.hollyvoc.qc.bean.CustcontentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shade.storm.org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hollycrm.hollyvoc.constant.Constant.*;
import static com.hollycrm.hollyvoc.qc.QCConstant.*;

/**
 * Created by qianxm on 2017/12/6.
 */
public class DataParseBolt implements IBasicBolt {

    public final static String NAME = "data-parse-bolt";

    private static Logger logger = LoggerFactory.getLogger(DataProcess.class);
    private Map<Integer,String> filedHBaseMap;  // 字段-顺序
    private Map<String, Integer> qcFieldMap; // 质检字段顺序
    private AtomicInteger counter = new AtomicInteger(0);
    private static Map<String, Integer> txtFiledMap; // 录音信息字段-索引位置
    private static Integer sheetNoIdx;


    @Override
    public void prepare(Map stormConf, TopologyContext context) {
        System.out.println("dp prepare!");

        Map<String,Integer>  map = Constant.getHBaseMapping();
        filedHBaseMap = new HashMap<>(map.size());
        map.forEach((k, v) -> {
            filedHBaseMap.put(v, k);
        });
        // 质检字段顺序
        qcFieldMap = Constant.getQcMatchMapping();

        txtFiledMap = Constant.getHBaseMapping(); // 文本记录字段
        txtFiledMap.remove(userContent);
        txtFiledMap.remove(agentContent);
        txtFiledMap.remove(allContent);
        sheetNoIdx = txtFiledMap.get(sheetNo);

    }

    @Override
    public void execute(Tuple input, BasicOutputCollector collector) {

        if(TOPOLOGY_STREAM_QC_ID.equals(input.getSourceStreamId())){
            try {
                String rowKey;
                String key = ByteUtil.getStringFromByteArray(input.getBinaryByField(KafkaConstant.KEY));
                String value= ByteUtil.getStringFromByteArray(input.getBinaryByField(KafkaConstant.VALUE));

                String[] keys = key.split(DELIMITER_FIELDS); // 对key进行处理，如果是普通数据，key=rowkey，异常数据key=rowkey#标识符
                String mark = ""; // 数据标识
                String streamId = TOPOLOGY_STREAM_QC_ID; // 默认是正常数据
                Values values = new Values(); // 默认的处理方式
                if(keys.length==2){
                    rowKey = keys[0];
                    mark = keys[1];
                } else {
                    // 正常数据
                    rowKey = key;
                    values = txtPretreat(rowKey, value, filedHBaseMap);
                }

                try {
                    switch (mark) {
                        case ORACLE_MARK:
                            streamId = TOPOLOGY_ORACLE_ID;
                            //  todo 解析oracle保存失败的数据
                            values = qcErrPretreat(rowKey, value, qcFieldMap);
                            break;
                        case QUALITY_MARK:
                            streamId = TOPOLOGY_STREAM_QC_ID;
                            values = txtPretreat(rowKey, value, filedHBaseMap);
                            break;
                    }

                    // 有数据才会发送给下游，否则不发送数据
                    if(values.size()>0){
                        // 发送数据到下游
                        emitData(streamId, values, collector);

                        logger.info(" pro " + rowKey.substring(10, 12) + " dataprocess-emit index :" + counter.incrementAndGet());

                    }

                } catch (Exception e) {
                    // 属于代码异常
                    logger.error("spout has error", e);
                }
            } catch (Exception e) {
                logger.error(" error", e);
            }
        }

    }


    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
//        declarer.declareStream(
//                TOPOLOGY_STREAM_QC_ID,
//                new Fields(DEC_ROW_KEY, DEC_PROVINCE, DEC_DAY,
//                        DEC_BASIC_INFO, DEC_USER_TXT, DEC_AGENT_TXT, DEC_ALL_TXT)
//        );

        declarer.declareStream(
                TOPOLOGY_STREAM_QC_ID,
                new Fields(DEC_ROW_KEY, CUNSTINFO)
        );
        // 发送到oracle-bolt
        declarer.declareStream(TOPOLOGY_ORACLE_ID, new Fields(
                qcId, qcName, id, qcType, qcTxtContent, qcMatchWord,
                province, custBrand, satisfication, businessType,
                silenceLength, userCode,acceptTime,caller,callee));


    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    private void emitData(String streamId, Values values, BasicOutputCollector collector) {
        collector.emit(streamId,values);
    }


    /**
     * 处理正常数据、hbase异常数据、index异常数据。
     * @param value 文本信息
     * @param rowKey rowKey
     */
    private static Values txtPretreat(String rowKey, String value, Map<Integer,String> filedHBaseMap){

        String[] infos = value.split("\\" + DELIMITER_PIPE); // “|”切分数据是需要转译
        String userTxt="",agentTxt="",allTxt="";
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<infos.length;i++){
            // 找到文本的值添
            String val = infos[i];
            String file = filedHBaseMap.get(i);
            if(userContent.equals(file)){
                userTxt = val;
                continue;
            }
            if(agentContent.equals(file)){
                agentTxt = val;
                continue;
            }
            if(allContent.equals(file)){
                allTxt = val;
                continue;
            }
            builder.append(val).append(DELIMITER_PIPE); // 个字段之间用“|”拼接
        }
        String basicInfo = builder.substring(0,builder.length()-1);
        String[] basicInfos = basicInfo.split("\\" + DELIMITER_PIPE);

        long duration =  Long.parseLong(basicInfos[txtFiledMap.get(recordLength)]);
        long silence = Long.parseLong(basicInfos[txtFiledMap.get(silenceLength)]);

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
                basicInfos[txtFiledMap.get(silenceLength)], ConstUtils.getRecoinfoLengthRangeCode(duration),
                ConstUtils.getSilenceRangeCode(duration, silence),
                userContent, agentContent, allContent, basicInfos[txtFiledMap.get(direction)],
                basicInfos[txtFiledMap.get(recordFormat)], basicInfos[txtFiledMap.get(recordSampRate)],
                basicInfos[txtFiledMap.get(recordEncodeRate)], hasSheet,
                basicInfos[txtFiledMap.get(netType)], basicInfos[txtFiledMap.get(hour)]);
        // 发送到下游的数据,如果rowkey格式有问题，省份截取不到，在下游的index就会出错
        return new Values(rowKey, custs);

    }

    /**
     * 针对质检保存到数据库异常的数据.
     * @param rowKey rowkey
     * @param value 异常数据信息
     * @param fileds  字段-顺序集合
     * @return 返回要发送到下游的数据
     */
    private static Values qcErrPretreat(String rowKey, String value, Map<String, Integer> fileds){
        String[] qc = value.split(DELIMITER_FIELDS); // “#”切分数据是需要转译
//        System.out.println("qc " + qc.length +" " + StringUtils.join(qc, "-"));
        return new Values(
                qc[fileds.get(qcId)], qc[fileds.get(qcName)], qc[fileds.get(Constant.rowkey)],
                qc[fileds.get(qcType)], getValues(qc[fileds.get(qcTxtContent)]),
                getValues(qc[fileds.get(qcMatchWord)]),getValues(qc[fileds.get(province)]),
                getValues(qc[fileds.get(custBrand)]), getValues(qc[fileds.get(satisfication)]),
                getValues(qc[fileds.get(businessType)]), getValues(qc[fileds.get(silenceLength)]),
                getValues(qc[fileds.get(userCode)]), getValues(qc[fileds.get(acceptTime)]),
                getValues(qc[fileds.get(caller)]), getValues(qc[fileds.get(callee)]));
    }

    private static String getValues(String val) {
        return StringUtils.isEmpty(val)?" ":val;
    }
}
