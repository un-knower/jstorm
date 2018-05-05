package com.hollyvoc.data.pretreat.pares.match.meta;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by zhaihw on 2017/2/9.
 * 接触记录
 */
@Setter @Getter
public class ContactMeta {
    private String callId;
    private String contactId;
    private String agentCode;
    private String direction;
    private String caller;
    private String callee;
    private String mobileNo;
    private String acceptTime;
    private String custArea;
    private String custBand;
    private String custLevel;
    private String startTime;
    private String recordLength;
    private String ivrTrack;
    private String queue;
    private String queueStartTime;
    private String queueEndTime;
    private String answerStartTime;
    private String answerEndTime;
    private String talkingStartTime;
    private String talkingEndTime;
    private String satisfaction;
}
