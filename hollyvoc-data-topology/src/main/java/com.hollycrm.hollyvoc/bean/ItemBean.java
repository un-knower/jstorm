package com.hollycrm.hollyvoc.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by qianxm on 2017/12/6.
 * xml文件中每个item的内容
 */
@Setter
@Getter
public class ItemBean {
    private Long startTime; // 通话开始时间
    private Long endTime; // 通话结束时间
    private double energy; // 情感值
    private double speed; // 语速
    private String text; // 通话内容

    public ItemBean(Long startTime, Long endTime, double energy, double speed, String text) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.energy = energy;
        this.speed = speed;
        this.text = text;
    }
}
