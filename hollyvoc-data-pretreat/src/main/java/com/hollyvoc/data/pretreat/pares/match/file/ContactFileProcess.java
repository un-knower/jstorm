package com.hollyvoc.data.pretreat.pares.match.file;

import com.hollyvoc.data.pretreat.config.Const;
import com.hollyvoc.data.pretreat.pares.match.meta.ContactMeta;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.hollyvoc.data.pretreat.config.Const.CODE_VALUE_ERR;
import static com.hollyvoc.data.pretreat.config.Const.CodeType.*;


/**
 * Created by zhaihw on 2017/2/9.
 *
 */
@Log4j
public class ContactFileProcess extends ListStructuredFileProcess<ContactMeta> {

    @Override
    ContactMeta assign(String line, Map<Const.CodeType, Map<String, String>> maps) {
        String[] lines = line.split("\\|");
        if (lines.length == 22) {
            ContactMeta meta = new ContactMeta();
            int i = 0;
            meta.setCallId(lines[i++]);
            meta.setContactId(lines[i++]);
            meta.setAgentCode(lines[i++]);
            meta.setDirection(lines[i++]);
            meta.setCaller(lines[i++]);
            meta.setCallee(lines[i++]);
            meta.setMobileNo(lines[i++]);
            meta.setAcceptTime(lines[i++]);
            meta.setCustArea(checkCode(lines[i++], maps.get(AREA_TREE)));
            meta.setCustBand(checkCode(lines[i++], maps.get(CUST_BAND)));
            meta.setCustLevel(checkCode(lines[i++], maps.get(CUST_LEVEL)));
            meta.setStartTime(lines[i++]);
            meta.setRecordLength(lines[i++]);
            meta.setIvrTrack(lines[i++]);
            meta.setQueue(lines[i++]);
            meta.setQueueStartTime(lines[i++]);
            meta.setQueueEndTime(lines[i++]);
            meta.setAnswerStartTime(lines[i++]);
            meta.setAnswerEndTime(lines[i++]);
            meta.setTalkingStartTime(lines[i++]);
            meta.setTalkingEndTime(lines[i++]);
            meta.setSatisfaction(checkCode(lines[i], maps.get(SATISFACTION)));
            return meta;
        } else {
            log.warn("contact ERR " + line);
        }
        return null;
    }

    private String checkCode(String code, Map<String, String> maps) {
        if(StringUtils.isEmpty(code)) return CODE_VALUE_ERR;
        String name = maps.get(code);
        if(StringUtils.isEmpty(name)) return CODE_VALUE_ERR;
        return code;
    }
}
