package com.hollycrm.hollyvoc.constant;

/**
 * Created by alleyz on 2017/5/17.
 *
 */
public class ConstUtils {
    private ConstUtils(){}
    /**
     * 求取录音时长区间 根据常用编码"RECOINFO_LENGTH"
     */
    public static String getRecoinfoLengthRangeCode(long time){
        int length = (int) Math.ceil(time / (double) 1000);
        if(length<=35)return "01";
        if(length<=95 && length>35)return "02";
        if(length<=180 && length > 95)return "03";
        if(length<=300 && length>180)return "04";
        if(length>300)return "05";
        return "unKnow";
    }
    /**
     * 获取静音区间 编码”SILENCE_LENGTH“
     */
    public static String getSilenceRangeCode(long recoinfoLength,long silenceLength){
        if(recoinfoLength==0)return "unKnow";
        double r=silenceLength/(double)recoinfoLength;
        if(r<=0.4)return "1";
        else return "2";
    }

    private static int count=0;

    /**
     * 计数器自增.
     */
    private static synchronized void countIncrement(){
        count++;
        if(count > 9999) count = 0;
    }

    /**
     * 获取唯一主键
     * @return 当前毫秒数+计数器
     */
    public static String javaId() {
        countIncrement();
        return System.currentTimeMillis() + "" + count;
    }
}
