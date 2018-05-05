package com.hollyvoc.data.pretreat.pares.voice.parsing;


import com.hollyvoc.data.pretreat.pares.voice.bean.ParseResult;

/**
 * Created by zhaihw on 2016/3/9.
 * xml解析类接口
 */
public interface IVoiceParse {
    /**
     * 句子分隔符
     */
    public final static String senMark="|";
    /**
     * 用户分隔符
     */
    public final static String userMark="n1#";
    /**
     * 坐席分隔符
     */
    public final static String agentMark="n0#";
    /**
     * 解析
     */
    ParseResult parse() throws Exception;
}
