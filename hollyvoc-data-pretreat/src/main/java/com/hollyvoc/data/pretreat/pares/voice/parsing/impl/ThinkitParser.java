package com.hollyvoc.data.pretreat.pares.voice.parsing.impl;


import com.hollyvoc.data.pretreat.pares.voice.bean.ParseResult;
import com.hollyvoc.data.pretreat.pares.voice.bean.WordBean;
import com.hollyvoc.data.pretreat.pares.voice.bean.thinkit.*;
import com.hollyvoc.data.pretreat.pares.voice.parsing.IVoiceParse;
import org.apache.log4j.Logger;

import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaihw on 2016/3/9.
 * 中科信利xml的解析
 */
public class ThinkitParser implements IVoiceParse {
    private static Logger logger = Logger.getLogger(ThinkitParser.class);
    private InputStream is;
    private Unmarshaller unmarshaller;
    private boolean isParseWord = false; //是否解析词语时间对象

    public ThinkitParser(InputStream is, Unmarshaller unmarshaller, boolean isParseWord) {
        this.is = is;
        this.unmarshaller = unmarshaller;
        this.isParseWord = isParseWord;
    }

    /**
     * 解析
     */
    @Override
    public ParseResult parse() throws Exception{
        ParseResult pr = new ParseResult();
        Root root = (Root) unmarshaller.unmarshal(is);
        OkList okList = root.getOkList();
        if (okList == null)
            return null;
        Record record = okList.getRecord();
        pr.setWaveUri(record.getRecordKey());//设置录音文件信息
        pr.setDuration(record.getDurtime().longValue());//设置录音长度
        SilenceList silenceList = record.getSilenceList();
        pr.setSilence(silenceList.getTotalTime().longValue());//设置静音时长
        SentenceList sentenceList = record.getSentenceList();
        StringBuilder user = new StringBuilder();
        StringBuilder agent = new StringBuilder();
        StringBuilder all = new StringBuilder();
        List<Sentence> sentences = sentenceList.getSentence();
        if (sentences != null) {
            long prevRole = -1L;//上次说话的角色
            for (Sentence sentence : sentences) {
                String text = sentence.getText();
                String separator;
                if (sentence.getRole().longValue() == prevRole) {
                    separator = senMark;
                } else {
                    if (1l == sentence.getRole().longValue())
                        separator = agentMark;
                    else
                        separator = userMark;
                }
                prevRole = sentence.getRole().longValue();

                if (1l == sentence.getRole().longValue()) {
                    agent.append(text).append(senMark);
                } else {
                    user.append(text).append(senMark);
                }
                if (isParseWord) { //是否解析词信息  生成波形图片需要
                    if (pr.getWordTimes() == null) {
                        pr.setWordTimes(new ArrayList<WordBean>());
                    }
                    pr.getWordTimes().addAll(parseWord(sentence.getWordList(), sentence.getRole().intValue()));
                }
                all.append(separator).append(text);
            }
            pr.setAgentText(agent.toString());
            pr.setUserText(user.toString());
            pr.setAllText(all.toString());
        }
        return pr;
    }

    /**
     * 解析出词语及其开始结束时间（生成录音波形图的时候使用）
     *
     * @param wl wordList节点
     * @return @see{com.hollycrm.textminer.voice.bean.WordBean}
     */
    private List<WordBean> parseWord(WordList wl, int role) {
        List<Word> words = wl.getWord();
        List<WordBean> wbs = new ArrayList<>(words.size());
        for (Word word : words) {
            WordBean wb = new WordBean();
            wb.setRole(role);
            wb.setText(word.getText());
            wb.setEndTime(word.getEndTime().intValue());
            wb.setStartTime(word.getStartTime().intValue());
            wbs.add(wb);
        }
        return wbs;
    }
}
