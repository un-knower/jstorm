package com.hollycrm.hollyvoc.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alleyz on 2017/5/16.
 * 常量类
 */
public class Constant {
    private Constant(){}
    public final static String TXT_MSG_TOPIC = "topic-origin";
    public final static String TXT_TOPIC = "test";
    public final static String TXT_STREAM_TOPIC = "qianxm-test";

    // 测试
    public final static String BASIC_TOPIC = "basic-topic",
                                BATCH_TOPIC = "batch-topic";
    public final static String ERR_TOPIC = "error-topic";
    public final static String QC_TOPIC = "qc-topic";
    public final static String QC_ERR_TOPIC = "qc-error-topic";
//    public final static String INDEX_ERR_TOPIC = "index-err-topic";

    public final static String TXT_MSG_GROUP_TOPOLOGY = "jstorm-topology";
    public final static String MSG_GROUP = "kafka.msg.group";
    public final static String KAFKA_TOPIC = "kafka.error.topic";


    //消息区块分割符
    public final static String DELIMITER_BLOCK = "&";
    // 消息字段分割符
    public final static String DELIMITER_FIELDS = "#";

    // 消息字段分割符
    public final static String DELIMITER_PIPE = "|";

    // 词性的分割符
    public final static String WORD_DELIMITER = "/";

    public static final String id="id";//主键
    public static final String custinfoId="custcontinfoId";//主键
    public static final String areaCode="areaCode";//省份
    public static final String custArea="custArea";//客户地市
    public static final String custLevel="custLevel";//客户级别
    public static final String custBrand="custBand";//品牌
    public static final String satisfication="satisfication";//满意度
    public static final String businessType="businessType";//业务类型
    public static final String userCode="userCode";//坐席
    public static final String week="week";//周
    public static final String acceptTime="acceptTime";//受理时间 yyyy-MM-dd HH:mm:ss
    public static final String recordLength="recoinfoLength";//录音时长
    public static final String silenceLength="silenceLength";//静音时长
    public static final String recordLengthRange="recordLengthRange";
    public static final String silenceLengthRange="silenceLengthRange";//静音时长区间
    public static final String year="year";//年份
    public static final String month="month";//月份
    public static final String day="day";//日期
    public static final String userContent="txtContentUser";//客户语音文本
    public static final String agentContent="txtContentAgent";//坐席语音文本
    public static final String allContent="txtContent";//全部文本
    public static final String mobileNo="mobileNo";
    public static final String caller="caller";//主叫
    public static final String callee="callee";//被叫
    public static final String serviceType="serviceType";
    public static final String direction = "direction";
    public static final String recordName="recordName";
    public static final String recordFormat="recordFormat"; //录音格式
    public static final String recordSampRate="recordSampRate"; // 采样率
    public static final String recordEncodeRate="recordEncodeRate"; // 编码率
    public static final String hasSheet = "hasSheet"; // 是否用工单

    public static final String netType="netType";//网别 联通 虚商 移动 电信
    public static final String queue="queue";//人工队列
    public static final String sheetType="sheetType";
    public static final String sheetNo="sheetNo";
    public static final String hour = "hour"; // 小时

    /**
     * 质检项相关字段
     */
    public static final String qcName = "qcName"; // 质检项名称
    public static final String qcType = "qcType"; // 质检类型
    public static final String qcCreator = "qcCreator"; // 创建人
    public static final String qcTxtContent = "qcTxtContent"; // 质检项内容
    public static final String qcTxtReg="qcTxtReg"; // 质检项内容正则表达式
    public static final String qcRole = "qcRole"; // 角色
    public static final String province = "province"; // 省份

    public static final String qcId = "qcId";
    public static final String qcMatchWord = "macthWord";
    public static final String rowkey = "rowKey";


    // hbase 保存字段
    private static final Map<String, Integer> FILED_HBASE_MAPPING = new HashMap<>();
    // 需要质检的字段的名称以及位置
    private static final Map<String, Integer> FILED_QC_ITEM_MAPPING = new HashMap<>();
    // 质检匹配到的字符串
    private static final Map<String, Integer> FILED_QC_MATCH_MAPPING = new HashMap<>();
    static {
        int i = 0;
        FILED_HBASE_MAPPING.put(custinfoId, i++);
        FILED_HBASE_MAPPING.put(areaCode, i++);
        FILED_HBASE_MAPPING.put(userCode, i++);
        FILED_HBASE_MAPPING.put(caller, i++);
        FILED_HBASE_MAPPING.put(callee, i++);
        FILED_HBASE_MAPPING.put(mobileNo, i++);
        FILED_HBASE_MAPPING.put(acceptTime, i++);
        FILED_HBASE_MAPPING.put(year, i++);
        FILED_HBASE_MAPPING.put(month, i++);
        FILED_HBASE_MAPPING.put(week, i++);
        FILED_HBASE_MAPPING.put(day, i++);
        FILED_HBASE_MAPPING.put(custArea, i++);
        FILED_HBASE_MAPPING.put(custBrand, i++);
        FILED_HBASE_MAPPING.put(satisfication, i++);
        FILED_HBASE_MAPPING.put(queue, i++);
        FILED_HBASE_MAPPING.put(serviceType, i++);
        FILED_HBASE_MAPPING.put(sheetNo, i++); //无需索引
        FILED_HBASE_MAPPING.put(sheetType, i++);
        FILED_HBASE_MAPPING.put(netType, i++);
        FILED_HBASE_MAPPING.put(recordName, i++); //无需索引
        FILED_HBASE_MAPPING.put(businessType, i++);
        FILED_HBASE_MAPPING.put(custLevel, i++);
        FILED_HBASE_MAPPING.put(direction, i++);
        FILED_HBASE_MAPPING.put(recordEncodeRate, i++);
        FILED_HBASE_MAPPING.put(recordSampRate, i++);
        FILED_HBASE_MAPPING.put(recordFormat, i++);
        FILED_HBASE_MAPPING.put(silenceLength, i++);
        FILED_HBASE_MAPPING.put(recordLength, i++ );
        FILED_HBASE_MAPPING.put(hour, i++);
        // 文本
        FILED_HBASE_MAPPING.put(userContent,i++);
        FILED_HBASE_MAPPING.put(agentContent, i++);
        FILED_HBASE_MAPPING.put(allContent, i);
//        // todo 添加情感值
//        FILED_HBASE_MAPPING.put()

        // 质检字段
        int j = 0;
        FILED_QC_ITEM_MAPPING.put(qcName, j++); // 质检名称
        FILED_QC_ITEM_MAPPING.put(qcType, j++); // 质检类型
        FILED_QC_ITEM_MAPPING.put(qcRole, j++); // 角色
        FILED_QC_ITEM_MAPPING.put(qcTxtContent, j++); // 质检内容
        FILED_QC_ITEM_MAPPING.put(qcTxtReg, j++); // 质检内容正则表达式
        FILED_QC_ITEM_MAPPING.put(qcCreator, j++); // 质检项创建人
        FILED_QC_ITEM_MAPPING.put(areaCode, j++); // 省份
        FILED_QC_ITEM_MAPPING.put(custBrand, j++); // 品牌
        FILED_QC_ITEM_MAPPING.put(custLevel, j++); // 级别
        FILED_QC_ITEM_MAPPING.put(satisfication, j++); // 满意度
        FILED_QC_ITEM_MAPPING.put(businessType, j++); // 业务类型
        FILED_QC_ITEM_MAPPING.put(direction, j++); // 呼叫方向
        FILED_QC_ITEM_MAPPING.put(recordLength, j++); // 通话时长
        FILED_QC_ITEM_MAPPING.put(silenceLength, j++); // 静音时长
        FILED_QC_ITEM_MAPPING.put(userCode, j); // 坐席工号

        // 质检匹配到的字符串
        int q = 0;
//        javaId(),qcItemId,itemName,rowkey,type,content,matchword,prov,qcCustBrand
//                ,satisfication,businessType,silenceLength,userCode,acceptTime,caller,callee
        FILED_QC_MATCH_MAPPING.put(id, q++); // 保存到数据库中的记录id
        FILED_QC_MATCH_MAPPING.put(qcId, q++);  // 质检项id
        FILED_QC_MATCH_MAPPING.put(qcName, q++);
        FILED_QC_MATCH_MAPPING.put(rowkey, q++); // rowKey
        FILED_QC_MATCH_MAPPING.put(qcType, q++);
        FILED_QC_MATCH_MAPPING.put(qcTxtContent, q++);
        FILED_QC_MATCH_MAPPING.put(qcMatchWord, q++);
        FILED_QC_MATCH_MAPPING.put(province, q++);
        FILED_QC_MATCH_MAPPING.put(custBrand, q++);
        FILED_QC_MATCH_MAPPING.put(satisfication, q++);
        FILED_QC_MATCH_MAPPING.put(businessType, q++);
        FILED_QC_MATCH_MAPPING.put(silenceLength, q++);
        FILED_QC_MATCH_MAPPING.put(userCode, q++);
        FILED_QC_MATCH_MAPPING.put(acceptTime, q++);
        FILED_QC_MATCH_MAPPING.put(caller, q++);
        FILED_QC_MATCH_MAPPING.put(callee, q);
    }
    public static Map<String, Integer> getHBaseMapping() {
        return new HashMap<>(FILED_HBASE_MAPPING);
    }
    public static Map<String, Integer> getQcMapping() {
        return new HashMap<>(FILED_QC_ITEM_MAPPING);
    }
    public static Map<String, Integer> getQcMatchMapping(){return new HashMap<>(FILED_QC_MATCH_MAPPING);}



    public final static String HBASE_ZK_QUORUM = "hbase.zookeeper.quorum";
    public final static String HBASE_ZK_QUORUM_PORT = "hbase.zookeeper.property.clientPort";

    // hbase 表
    public static final String CUST_INFO_H_TABLE = "custcontinfo";
    // 测试表名称
    public static final String CUST_INFO_H_TEST_TABLE = "custcontinfo";

    public static final String CUST_INFO_H_FAMILY = "info";
    public static final String CUST_INFO_H_QUA = "contact";
    public static final String CUST_TXT_H_FAMILY = "txt";
    public static final String CUST_ALL_TXT_H_QUA = allContent;
    public static final String CUST_AGENT_TXT_H_QUA = agentContent;
    public static final String CUST_USER_TXT_H_QUE = userContent;

    public static final String REDIS_KEY_NEW_WORD = "JNW";
    public static final String REDIS_KEY_HOT_WORd = "JHW";

    /**
     * z消费时的偏移位置 有三种取值 latest earliest none
     */
    public final static String OFFSET_RESET = "auto.offset.reset";

    /**
     * 消费时的偏移位置
     */
    public enum OffsetReset{
        LATEST("latest"), EARLIEST("earliest"), NONE("none");
        public String val;
        OffsetReset(String val) {
            this.val = val;
        }
    }

    /**
     * 文本角色
     */
    public static final String allRole="1";//全部
    public static final String userRole="3";//用户
    public static final String agentRole="2";//坐席


}
