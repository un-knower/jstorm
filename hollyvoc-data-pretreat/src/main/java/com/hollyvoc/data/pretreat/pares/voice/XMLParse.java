package com.hollyvoc.data.pretreat.pares.voice;


import com.hollyvoc.data.pretreat.config.Config;
import com.hollyvoc.data.pretreat.pares.voice.bean.ParseResult;
import com.hollyvoc.data.pretreat.pares.voice.parsing.IVoiceParse;

import java.io.FileInputStream;

import static com.hollyvoc.data.pretreat.pares.voice.parsing.factory.VoiceParserFactory.getInstance;

/**
 * Created by qianxm on 2017/12/13.
 */
public class XMLParse {

    public static void main(String[] args) {
        try {
            String xmlFile = "D:\\myWork\\智能语音总部环境配置\\智能语音总部系统应用明细\\数据\\71-湖北\\20171114\\20171114\\08\\0A54BCCDAD72451CA694B3E349D036D4.xml";
            IVoiceParse xmlParser = getInstance().pointParser(Config.getVal("voiceParser"), new FileInputStream(xmlFile), false);
            ParseResult pr = xmlParser.parse();
            System.out.println( pr.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
