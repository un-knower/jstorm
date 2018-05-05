package com.hollycrm.hollyvoc.constant;

/**
 * Created by qianxm on 2017/7/6.
 */
public class TopoConstant {

    public final static String TOPOLOGY_NAME = "topology.name"; // topology name
    public final static String KAFKA_SPOUT = "kafka-spout"; // kafka spout name
    /**
     * spout/bolt 并行名称
     */
    public final static String TOPOLOGY_PARALLEL_SPOUT = "spout.parallel";
    public final static String TOPOLOGY_PARALLEL_BOLT_HBASE = "bolt.parallel.hbase",
                        TOPOLOGY_PARALLEL_BOLT_HBASE_ERR = "bolt.parallel.hbase.err";

    public final static String TOPOLOGY_PARALLEL_BOLT_INDEX = "bolt.parallel.index";
    public final static String TOPOLOGY_PARALLEL_QC = "bolt.parallel.qc";
    public final static String TOPOLOGY_PARALLEL_ORACLE = "bolt.parallel.oracle";
    public final static String TOPOLOGY_PARALLEL_ERROR = "bolt.parallel.error";
    public final static String TOPOLOGY_PARALLEL_NW = "bolt.parallel.nw";
    public final static String TOPOLOGY_PARALLEL_NW_REDIS = "bolt.parallel.nw.redis";



    /**
     * spout/bolt id
     */
    public final static String TOPOLOGY_STREAM_TXT_ID = "txt-spout-stream";
    public final static String TOPOLOGY_STREAM_HBASE_ID = "hbase-stream",
                        TOPOLOGY_STREAM_HBASE_COMMIT_ERR_ID = "hbase-commit-error", // 提交失败id
                        TOPOLOGY_STREAM_HBASE_TYPE_ERR_ID = "hbase-type-error"; // 数据格式异常id
    public final static String TOPOLOGY_STREAM_INDEX_ERR_ID = "index-error";
    public final static String TOPOLOGY_STREAM_QC_ID = "qc-stream";

    public final static String TOPOLOGY_STREAM_TXT_BACTH_ID = "txt-batch-spout-stream";

    public final static String TOPOLOGY_STREAM_NW_ID = "new-word-stream"; // 新词发现
    public final static String TOPOLOGY_STREAM_NW_ERR_ID = "new-word-error-stream"; // 发现新词异常
    public final static String TOPOLOGY_STREAM_NW2REDIS_ERR_ID = "new-word-2redis-error"; // 保存新词到redis异常


    /**
     * spout 发送到bolt的字段
     */
    public final static String DEC_BATCH_ID = "batchId";
    public final static String DEC_ROW_KEY = "rowKey";
    public final static String DEC_BASIC_INFO = "basicInfo";
    public final static String DEC_PROVINCE = "province";
    public final static String DEC_DAY = "day";
    public final static String DEC_AGENT_TXT = "agentTxt";
    public final static String DEC_USER_TXT = "userTxt";
    public final static String DEC_ALL_TXT = "allTxt";

//    public final static long HBASE_TIMEOUT = 120;
//    public final static int HABSE_BATCHSIZE = 1000;
    public final static long HBASE_TIMEOUT = 5; // 秒
    public final static int HABSE_BATCHSIZE = 1000;
    public final static int ORACLE_BATCHSIZE = 1000;
    public final static int SOLR_BATCHSIZE = 1000;
    public final static int TIME_OUT = 2; // 超时时间1秒



    /**
     * 异常数据标识符
     */
    public final static String HBASE_MARK = "h", // hbase 异常数据
            INDEX_MARK = "i", // 索引异常数据
            QUALITY_MARK ="q", // 质检异常数据
//            ORACLE_MARK = "o"; // 保存oracle数据异常
            NW_MARK = "n", // 学习新词异常
            NW_REDIS_MARK = "r"; // 新词保存到redis异常

    /**
     * redis 质检项 key值
     */
    public final static String REDIS_QC_ITEM = "_$holly_qc_item$_",
                                REDIS_QC_VESION = "_$holly_qc_version$_";

    public final static String TIME_KEY = "time";

    /**
     * 新词学习dubbo相关参数
     */
    public final static String DUBBO_PROTOCOL = "zookeeper";
    public final static int CONTENT_BATCH_NUM =50; // 批量新词学习录音文本数量
    public static final String DEC_WORD = "new_word";
    public static final String DEC_FREQ = "word_freq";
    public final static String REDIS_NW_KEY = "NewWordByStream";



}
