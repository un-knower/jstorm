package com.hollyvoc.data.pretreat.pares.voice.bean;

/**
 * Created by zhaojy on 2016/4/7.
 *
 * 词语文本以及其对应的开始时间和结束时间
 */
public class WordBean {

    /**
     * 角色.
     */
    private int role;
    /**
     * 开始时间.
     */
    private int startTime;

    /**
     * 结束时间.
     */
    private int endTime;

    /**
     * 文本.
     */
    private String text;

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
