package com.hollycrm.hollyvoc.qc.test;

import com.hollycrm.hollyvoc.qc.regular.TxtMatch;
import com.hollycrm.hollyvoc.constant.Constant;
import shade.storm.org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.hollycrm.hollyvoc.qc.QCConstant.*;
import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/9/22.
 */
public class QcTests {
    private static Map<String, Integer> txtFiledMap; // 录音信息字段-索引位置
    private static Map<String, Integer> qcItmeFiledMap; // 质检字段-索引位置
    private static Map<String, Integer> qcFiledMap; // 需要质检的字段


    private ConcurrentHashMap<String, String> qcItems = new ConcurrentHashMap<>(); // redis 中的质检项 key-质检id、value 存放质检项
    private TxtMatch txtMatch;


    public QcTests() {
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
//        qcItems.putAll(RedisHelper.getInstance().getHashByKey(REDIS_QC_ITEM));
        qcItems.put("15058009972594","流式处理质检测试#1#1#( 工信部 OR 通管局 OR 工商局 OR 报社 OR 曝光 OR 12315 OR 消委会 OR 消费委员会 ) NOT ( 人脸识别 OR 实名 OR 绿通 OR 加急 OR 优先处理 )# #qianxmZB#18#05# # # # # # # ");
        qcItems.put("15011386274981","流量争议#1#2#漫游 OR 停机 OR 流量#(漫游|停机|流量)#beijing1#11#02,03#11,04#02#01,02# #01,03#2# ");
//        qcItems.put("15018287317512","河北质检#1#1#满意 OR 坏人#(不满意|坏人)#admin#11,17,18# # # # # # #1# ");
//        质检项名称|质检类型|角色|文本内容|正则内容|创建人|省份|cust_band|cust_level|satisfication|business_type|direction|recoinfoLengith |silenceLength|agentCode|
        qcItems.put("15018287317512","河北质检#1#1#满意 OR 坏人#(不满意|坏人)#admin#11,17,18# # # # # # # # ");
        txtMatch = TxtMatch.getInstance();
    }

    private  void qc(Map<String,String> tuple){
        String rowKey = tuple.get(DEC_ROW_KEY);
        String basicInfo = tuple.get(DEC_BASIC_INFO);
        String agentTxt = tuple.get(DEC_AGENT_TXT);
        String userTxt = tuple.get(DEC_USER_TXT);
        String allTxt = tuple.get(DEC_ALL_TXT);
        String prov = tuple.get(DEC_PROVINCE);
        String[] basicInfos = basicInfo.split("\\" + DELIMITER_PIPE);
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
//                        System.out.println(field + "qcFiledMap  i" + i);
//                        int i = qcFiledMap.get(field);


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

//                            String words = "";


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
                                break; // 如果不满足条件，就对下一个质检项的条件进行匹配
                            }
                        }
                    }

            // for 循环执行之后没有继续执行。
            System.out.println(" one qcItem finish !");
            // 质检项中的文本内容不为空，所以只有匹配到数据的记录符合质检条件需要保存到数据库
            if(StringUtils.isEmpty(words)){
                return;
            }
            System.out.println(" match qcId " + k +" rowkey: " + rowKey +" matchTxt: " + words);

            // 全部通过的就是符合条件的，需要发送到下游，保存到数据库
            String qcName = qcFields[qcItmeFiledMap.get(Constant.qcName)];
            String qcCreator = qcFields[qcItmeFiledMap.get(Constant.qcCreator)];
            String qcType = qcFields[qcItmeFiledMap.get(Constant.qcType)];

            System.out.println(" 匹配结果：" + k+ qcName+
                    rowKey+ qcType+ qcCreator+ role+ qcFields[qcFiledMap.get(qcTxtContent)]+ words+ prov+
                    basicInfos[txtFiledMap.get(custBrand)]+ basicInfos[txtFiledMap.get(satisfication)]+
                    basicInfos[txtFiledMap.get(businessType)]+ basicInfos[txtFiledMap.get(silenceLength)]+
                    basicInfos[txtFiledMap.get(userCode)]+basicInfos[txtFiledMap.get(acceptTime)]+
                    basicInfos[txtFiledMap.get(caller)]+ basicInfos[txtFiledMap.get(callee)]);
            // 匹配到的数据才会发送
//            collector.emit(TOPOLOGY_ORACLE_ID, new Values(k, qcName,
//                    rowKey, qcType, qcCreator, role, qcFields[qcFiledMap.get(qcTxtContent)], words, prov,
//                    basicInfos[txtFiledMap.get(custBrand)], basicInfos[txtFiledMap.get(satisfication)],
//                    basicInfos[txtFiledMap.get(businessType)], basicInfos[txtFiledMap.get(silenceLength)],
//                    basicInfos[txtFiledMap.get(userCode)],basicInfos[txtFiledMap.get(acceptTime)],
//                    basicInfos[txtFiledMap.get(caller)], basicInfos[txtFiledMap.get(callee)]));
        });
    }


    public static void main(String[] args) {
      QcTests qc = new QcTests();
      Map<String, String> map = new HashMap<>();
      map.put(DEC_ROW_KEY,"0010307102181813620211");
      map.put(DEC_BASIC_INFO,"17030100000002390|11|17904|17603126924|10010|17603126924|20170301005756|2017|201703|9|20170301|187|02|04|que0|serv0||00|NO|recn0|06|10|00|4k|16bit|vox|14570|71050|2017030100");
      map.put(DEC_AGENT_TXT,"你好，流量为什么我的网它显示的是3g有订全年定期连接不上我。4g啊嗯说这个不用删是那个。今天网上还是行之后其实我姐夫是吧到今天中午应该在。可以吧哦，好得谢谢再见");
      map.put(DEC_USER_TXT,"您好，很高兴为您服务通话的这个号是吧唉，行稍等一下唉先生给带来不便了，我看了一下，现在暂时得话，您这不是上个月背风订了嘛，然后被关了数据功能，现在暂时还没有，及时更新给带来不便，所以您稍晚一会再试一下现在开始您可以随时试，因为现在我们确定不了，他什么时候给开就因为现在状态上还没有得话，然后给9封了，所以只能建议随时试一下呢嗯，对自动呢，不用做什么请问具体时间我们就是确定不了，您可以随时试一下唉，不客气感谢您的来电，如果服务满意，请按一祝您生活愉快再见嗯？|您好，很高兴为您服务。。你好，为什么我的网它显示的是3g有订全年定期连接不上我。4g啊！。通话的这个号是吧！。嗯？。唉，行稍等一下。。说这个不用删。。唉先生给带来不便了，我看了一下，现在暂时得话，您这不是上个月背风订了嘛，然后被关了数据功能，现在暂时还没有，及时更新给带来不便，所以您稍晚一会再试一下。。是那个。今天网上还是行之后。。现在开始您可以随时试，因为现在我们确定不了，他什么时候给开就因为现在状态上还没有得话，然后给9封了，所以只能建议随时试一下呢！。其实我姐夫是吧！。嗯，对自动呢，不用做什么。。到今天中午应该在。可以吧！。请问具体时间我们就是确定不了，您可以随时试一下。。哦，好得谢谢！。唉，不客气感谢您的来电，如果服务满意，请按一祝您生活愉快再见。。再见。。嗯？");
      map.put(DEC_ALL_TXT,"你好，为什么我的网它显示的是3g有订全年定期连接不上我。4g啊嗯说这个不用删是那个。今天网上还是行之后其实我姐夫是吧到今天中午应该在。可以吧哦，好得谢谢再见|您好，很高兴为您服务通话的这个号是吧唉，行稍等一下唉先生给带来不便了，我看了一下，现在暂时得话，您这不是上个月背风订了嘛，然后被关了数据功能，现在暂时还没有，及时更新给带来不便，所以您稍晚一会再试一下现在开始您可以随时试，因为现在我们确定不了，他什么时候给开就因为现在状态上还没有得话，然后给9封了，所以只能建议随时试一下呢嗯，对自动呢，不用做什么请问具体时间我们就是确定不了，您可以随时试一下唉，不客气感谢您的来电，如果服务满意，请按一祝您生活愉快再见嗯？|您好，很高兴为您服务。。你好，为什么我的网它显示的是3g有订全年定期连接不上我。4g啊！。通话的这个号是吧！。嗯？。唉，行稍等一下。。说这个不用删。。唉先生给带来不便了，我看了一下，现在暂时得话，您这不是上个月背风订了嘛，然后被关了数据功能，现在暂时还没有，及时更新给带来不便，所以您稍晚一会再试一下。。是那个。今天网上还是行之后。。现在开始您可以随时试，因为现在我们确定不了，他什么时候给开就因为现在状态上还没有得话，然后给9封了，所以只能建议随时试一下呢！。其实我姐夫是吧！。嗯，对自动呢，不用做什么。。到今天中午应该在。可以吧！。请问具体时间我们就是确定不了，您可以随时试一下。。哦，好得谢谢！。唉，不客气感谢您的来电，如果服务满意，请按一祝您生活愉快再见。。再见。。嗯？");
      map.put(DEC_PROVINCE,"18");

      qc.qc(map);
    }
}
