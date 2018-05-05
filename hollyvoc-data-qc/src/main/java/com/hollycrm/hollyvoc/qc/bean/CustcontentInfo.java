package com.hollycrm.hollyvoc.qc.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by qianxm on 2017/12/1.
 * 数据结构
 */
@Setter
@Getter
public class CustcontentInfo implements Serializable {


    protected  String custinfoId;//主键
    protected  String areaCode;//省份
    protected  String userCode;//坐席
    protected  String caller;//主叫
    protected  String callee;//被叫
    protected  String mobileNo; // 电话号码
    protected  String acceptTime;//受理时间 yyyy-MM-dd HH:mm:ss
    protected  String year;//年份
    protected  String month;//月份
    protected  String week;//周
    protected  String day;//日期
    protected  String custArea;//客户地市
    protected  String custBrand;//品牌
    protected  String satisfication;//满意度
    protected  String queue;//人工队列
    protected  String serviceType; // 服务类型

    protected  String sheetNo;
    protected  String sheetType;

    protected  String custLevel;//客户级别

    protected  String businessType;//业务类型



    protected  String recordLength;//录音时长
    protected  String silenceLength;//静音时长
    protected  String talkingLength;//通话时长
    protected  String recordLengthRange;
    protected  String silenceLengthRange;//静音时长区间


    protected  String userContent;//客户语音文本
    protected  String agentContent;//坐席语音文本
    protected  String allContent;//全部文本


    protected  String emotion;//情绪值

    protected  String direction;
    protected  String recordFile;
    protected  String recordName;

    protected  String recordFormat; //录音格式
    protected  String recordSampRate; // 采样率
    protected  String recordEncodeRate; // 编码率
    protected  String hasSheet; // 是否用工单

    protected  String netType;//网别 联通 虚商 移动 电信

    protected  String recordTransPath;
    protected  String recordTransName;

    protected  String hour; // 小时

    protected String userXml; // 用户的xml文件
    protected String agentXml; // 坐席的xml文件信息

    public CustcontentInfo() {
    }

    public CustcontentInfo(String custinfoId, String areaCode, String userCode, String caller, String callee, String mobileNo, String acceptTime, String year, String month, String week, String day, String custArea, String custBrand, String satisfication, String queue, String serviceType, String sheetNo, String sheetType, String custLevel, String businessType, String recordLength, String silenceLength, String recordLengthRange, String silenceLengthRange, String userContent, String agentContent, String allContent, String direction, String recordFormat, String recordSampRate, String recordEncodeRate, String hasSheet, String netType, String hour) {
        this.custinfoId = custinfoId;
        this.areaCode = areaCode;
        this.userCode = userCode;
        this.caller = caller;
        this.callee = callee;
        this.mobileNo = mobileNo;
        this.acceptTime = acceptTime;
        this.year = year;
        this.month = month;
        this.week = week;
        this.day = day;
        this.custArea = custArea;
        this.custBrand = custBrand;
        this.satisfication = satisfication;
        this.queue = queue;
        this.serviceType = serviceType;
        this.sheetType = sheetType;
        this.custLevel = custLevel;
        this.businessType = businessType;
        this.recordLength = recordLength;
        this.silenceLength = silenceLength;
        this.recordLengthRange = recordLengthRange;
        this.silenceLengthRange = silenceLengthRange;
        this.hasSheet = hasSheet;
        this.userContent = userContent;
        this.agentContent = agentContent;
        this.allContent = allContent;
        this.direction = direction;
        this.recordFormat = recordFormat;
        this.recordSampRate = recordSampRate;
        this.recordEncodeRate = recordEncodeRate;
        this.hasSheet = hasSheet;
        this.netType = netType;
        this.hour = hour;
    }

}
