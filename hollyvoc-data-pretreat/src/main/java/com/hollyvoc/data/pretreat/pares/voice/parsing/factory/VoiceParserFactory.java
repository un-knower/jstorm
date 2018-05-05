package com.hollyvoc.data.pretreat.pares.voice.parsing.factory;

import com.hollyvoc.data.pretreat.config.Const;
import com.hollyvoc.data.pretreat.pares.voice.parsing.IVoiceParse;
import com.hollyvoc.data.pretreat.pares.voice.parsing.impl.IflytekParser;
import com.hollyvoc.data.pretreat.pares.voice.parsing.impl.ThinkitParser;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * Created by zhaihw on 2016/3/9.
 * 录音xml解析工厂.
 */
public class VoiceParserFactory {
    private static Logger logger = Logger.getLogger(VoiceParserFactory.class);
    private static VoiceParserFactory factory = null;
    private VoiceParserFactory(){}
    private static Unmarshaller iflytekUnmarshaller;
    private static Unmarshaller thinkitUnmarshaller;
    public static String PKG_iflytekvoice = "com.hollycrm.voice.bean.iflytekvoice";
    public static String PKG_thinkit = "com.hollycrm.voice.bean.thinkit";

    public static String defaults = Const.defaultXmlVoiceParser;//默认的解析器
    static {
//        defaults = ConfigSupport.readValue("voiceParser");
        if(defaults == null)
            defaults = "thinkit";
        if("thinkit".equals(defaults)){
            getThinkitUnmarshaller(); // Unmarshaller
        }else if("iflytek".equals(defaults)){
            getIflytekUnmarshaller();
        }
    }
    /**
     * 采用DCL -懒汉单例
     * @return
     */
    public static VoiceParserFactory getInstance(){
        if(factory == null){
            synchronized (VoiceParserFactory.class){
                if(factory == null)
                    factory = new VoiceParserFactory();
            }
        }
        return factory;
    }

    /**
     * 获取解析对象
     * @param is
     * @return
     */
    public IVoiceParse defaultParser(InputStream is, boolean isParseWords)throws JAXBException{
        ParseType pt = null;
        if("iflytek".equals(defaults)){
            pt = ParseType.IFLYTEK;
        }else if("thinkit".equals(defaults)){
            pt = ParseType.THINKIT;
        }
        return defaultParser(pt, is, isParseWords);
    }

    public IVoiceParse pointParser(String parse, InputStream is,boolean isParseWords)throws JAXBException{
        ParseType pt = null;
        if("iflytek".equals(parse)){
            pt = ParseType.IFLYTEK;
        }else if("thinkit".equals(parse)){
            pt = ParseType.THINKIT;
        }
        return defaultParser(pt, is, isParseWords);
    }

    public IVoiceParse defaultParser(ParseType pt,InputStream is,boolean isParseWords)throws JAXBException{
        IVoiceParse parse = null;
        switch (pt){
            case IFLYTEK:
//                parse = getIflytekParse(is);
                parse = getIflytekParse(is, isParseWords);
                break;
            case THINKIT:
                parse = getThinkitParse(is,isParseWords);
                break;
        }
        return parse;
    }
    public enum ParseType{
        IFLYTEK,THINKIT
    }
    /**
     * 科大讯飞的解析器
     * @param is 输入流
     * @return 解析器对象
     * @throws JAXBException 包中类不存在可能导致此问题
     */
    private IVoiceParse getIflytekParse(InputStream is)throws JAXBException{
        if(iflytekUnmarshaller == null) throw new JAXBException("检查iflytek映射包是否正确");
        return new IflytekParser(is,iflytekUnmarshaller);
    }
    /**
     * 科大讯飞的解析器
     * @param is 输入流
     * @param isParseWords 是否解析词信息
     * @return 解析器对象
     * @throws JAXBException 包中类不存在可能导致此问题
     */
    private IVoiceParse getIflytekParse(InputStream is, boolean isParseWords)throws JAXBException{
        if(iflytekUnmarshaller == null) throw new JAXBException("检查iflytek映射包是否正确");
        return new IflytekParser(is,iflytekUnmarshaller, isParseWords);
    }
    /**
     * 中科信利的解析器
     * @param is 输入流
     * @return 解析器对象
     * @throws JAXBException 包中类不存在可能导致此问题
     */
    private IVoiceParse getThinkitParse(InputStream is,boolean isParseWords)throws JAXBException{
        if(thinkitUnmarshaller == null) throw new JAXBException("检查thinkit映射包是否正确");
        return new ThinkitParser(is,thinkitUnmarshaller,isParseWords);
    }

    /**
     * 初始Unmarshaller IflytekUnmarshaller
     */
    static void getIflytekUnmarshaller(){
        try {
            JAXBContext jc = JAXBContext.newInstance(PKG_iflytekvoice);
            iflytekUnmarshaller = jc.createUnmarshaller();//xml解析器
        }catch (JAXBException e){
            logger.error("解析xml出错",e);
        }
    }
    /**
     * 初始Unmarshaller ThinkitUnmarshaller
     */
    static void getThinkitUnmarshaller(){
        try {
            JAXBContext jc = JAXBContext.newInstance(PKG_thinkit);
            thinkitUnmarshaller = jc.createUnmarshaller();//xml解析器
        }catch (JAXBException e){
            logger.error("解析xml出错",e);
        }
    }

}
