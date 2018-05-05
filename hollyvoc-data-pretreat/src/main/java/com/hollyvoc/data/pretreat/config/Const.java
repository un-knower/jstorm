package com.hollyvoc.data.pretreat.config;

/**
 * Created by alleyz on 2017/5/17.
 */
public class Const {

    /**
     * 要转译的常用编码
     */
    public final static String CT_AREA_CODE = "AREA_CODE";
    public final static String CT_AREA_TREE = "AREA_TREE";
    public final static String CT_SHEET_TYPE = "SHEET_TYPE";
    public final static String CT_BUSINESS_TYPE = "BUSINESS_TYPE";
    public final static String CT_CUST_BRAND = "CUST_BRAND";
    public final static String CT_SATISFACTION = "SATISFACTION";
    public final static String CT_CUST_LEVEL = "CUST_LEVEL";
    public final static String CT_DIRECTION = "DIRECTION";
    public final static String CT_NET_TYPE = "NET_TYPE";
    public final static String CT_USE_CONTACT = "USE_CONTACT";



    // 需要翻译的类型
    public enum CodeType{
        DIRECTION, BUSINESS_TYPE, SHEET_TYPE, CUST_BAND, CUST_LEVEL, AREA_TREE, SATISFACTION, USE_CONTACT,
        AREA_CODE, NET_TYPE
    }

    public final static String CODE_VALUE_ERR = "ERR";
    public final static String CODE_VALUE_NAN = "00";

    public final static String REGEX_UNICOM = "^1((3[012])|(5[56])|(8[56])){1}\\d{8}$",
            REGEX_MOBILE = "^1((3[987654])|(5[012789])|(8[278])){1}\\d{8}$",
            REGEX_TELECOM = "^1(([35]3)|([8][019])){1}\\d{8}$",
            REGEX_MVNO_UNICOM = "^170[89]{1}\\d{7}$",
            REGEX_MVNO_MOBILE = "^1705\\d{7}$",
            REGEX_MVNO_TELECOM = "^1700\\d{7}$";

    // 网别类型
    public final static String NET_TYPE_UNICON = "U",
            NET_TYPE_MOBILE = "M", NET_TYPE_TELECOM = "T",
            NET_TYPE_MVNO_MOBILE = "VM",
            NET_TYPE_MVNO_TELECOM = "VT",
            NET_TYPE_MVNO_UNICON = "VU",
            NET_TYPE_MVNO_UNKNOW = "NO";



    //默认语音xml解析采用 中科信利
    // iflytek  thinkit
    public static String defaultXmlVoiceParser = "iflytek";

    //文件名称前缀 常量
    public final static String FN_CUSTCONTINFO = "CUSTCONTINFO"; //客户接触记录表标识
    public final static String FN_RECOINFO = "RECOINFO"; //录音记录信息
    public final static String FN_SEREQUEST = "SEREQUEST"; //服务请求表标识
}
