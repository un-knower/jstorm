package com.hollyvoc.data.pretreat.pares.voice.bean;


import java.util.List;

/**
 * Created by zhaihw on 2016/3/9.
 */
public class ParseResult {
    /**
     * 静音时长
     */
    private long silence;
    /**
     * 通话时长
     */
    private long duration;
    /**
     * 用户文本
     */
    private String userText;
    /**
     * 坐席文本
     */
    private String agentText;
    /**
     * 通话文本
     */
    private String allText;
    /**
     * 录音位置
     */
    private String waveUri;
    /**
     * 情绪值
     */
    private String emotion;

    /**
     * 词语时间
     */
    private List<WordBean> wordTimes;

    private Number agentSpeed;


    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public long getSilence() {
        return silence;
    }

    public void setSilence(long silence) {
        this.silence = silence;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUserText() {
        return userText;
    }

    public void setUserText(String userText) {
        this.userText = userText;
    }

    public String getAgentText() {
        return agentText;
    }

    public void setAgentText(String agentText) {
        this.agentText = agentText;
    }

    public String getAllText() {
        return allText;
    }

    public void setAllText(String allText) {
        this.allText = allText;
    }

    public String getWaveUri() {
        return waveUri;
    }

    public void setWaveUri(String waveUri) {
        this.waveUri = waveUri;
    }

    public List<WordBean> getWordTimes() {
        return wordTimes;
    }

    public void setWordTimes(List<WordBean> wordTimes) {
        this.wordTimes = wordTimes;
    }

    public Number getAgentSpeed() {
        return agentSpeed;
    }

    public void setAgentSpeed(Number agentSpeed) {
        this.agentSpeed = agentSpeed;
    }

    @Override
    public String toString() {
        return "ParseResult{" +
                "silence=" + silence +
                ", duration=" + duration +
                ", userText='" + userText + '\'' +
                ", agentText='" + agentText + '\'' +
                ", allText='" + allText + '\'' +
                ", waveUri='" + waveUri + '\'' +
                ", emotion='" + emotion + '\'' +
                ", wordTimes=" + wordTimes +
                ", agentSpeed=" + agentSpeed +
                '}';
    }
}
