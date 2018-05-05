package com.hollycrm.hollyvoc.qc;

/**
 * Created by qianxm on 2017/8/23.
 */
public class QCConstant {

    public final static String QC_SPOUT = "qc-spout"; // kafka spout name

    public final static String TOPOLOGY_STREAM_QC_ID = "qc-stream",
                                TOPOLOGY_STREAM_QC_ERR_ID = "qc-error-stream";
    public final static String TOPOLOGY_ORACLE_ID = "oracle-stream",
                                TOPOLOGY_ORACLE_ERR_ID = "orcl-error-stream";


    public final static int ORACLE_BATCHSIZE = 1000;
    public final static String ORACLE_MARK = "o", // 保存质检数据异常标志
                        QUALITY_MARK ="q"; // 质检异常数据标志
    public final static String TOPOLOGY_PARALLEL_QC = "bolt.parallel.qc";
    public final static String TOPOLOGY_PARALLEL_ORACLE = "bolt.parallel.oracle";
    public final static String TOPOLOGY_PARALLEL_SPOUT = "spout.parallel";
    public final static String  TOPOLOGY_PARALLEL_ERROR = "bolt.parallel.error";


    public final static String DEC_ROW_KEY = "rowKey";
    public final static String DEC_BASIC_INFO = "basicInfo";
    public final static String DEC_PROVINCE = "province";
    public final static String DEC_DAY = "day";
    public final static String DEC_AGENT_TXT = "agentTxt";
    public final static String DEC_USER_TXT = "userTxt";
    public final static String DEC_ALL_TXT = "allTxt";

    public final static String CUNSTINFO = "custinfos"; // 数据对象


    public final static int TIME_OUT = 2; // 超时时间1秒
    /**
     * redis 质检项 key值
     */
    public final static String REDIS_QC_ITEM = "_$holly_qc_item$_",
            REDIS_QC_VESION = "_$holly_qc_version$_";

    public final static String TIME_KEY = "time";

    public final static String INSERT_SQL =  "insert into tbl_voc_qc_list(row_id,qc_item_id," +
            "qc_item_name,custcontinfo_id,qc_item_type,qc_item_content," +
            "txt_content,area_code,cust_band,satisfication,business_type," +
            "silence_length,user_code,accept_time,caller,callee,is_true,domain_id)" +
            " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'0','sys')";
}
