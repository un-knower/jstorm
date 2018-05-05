package com.hollyvoc.data.pretreat.pares.match.meta;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by zhaihw on 2017/2/9.
 * 录音记录
 */
@Getter @Setter
public class RecordingMeta {
    private String recordNo;
    private String callId;
    private String recordStartTime;
    private String recordEndTime;
    private String recordLength;
    private String recordPath;
    private String recordFileName;
    private String recordFormat;
    private String recordSampRate;
    private String recordEncodeRate;
    private String caller;
    private String callee;
    private String contactId;
}
