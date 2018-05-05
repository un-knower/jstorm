package com.hollycrm.hollyvoc.qc.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by qianxm on 2017/12/6.
 * 记录每一录音转写后的x *
 */
@Getter @Setter
public class RecordInfo {
    protected String role;
    protected String count; // 说话次数
    protected Long duration; // 通过耗时
    protected List<ItemBean> contents; // 通话内容

    public RecordInfo(String role, String count, Long duration, List<ItemBean> contents) {
        this.role = role;
        this.count = count;
        this.duration = duration;
        this.contents = contents;
    }

}
