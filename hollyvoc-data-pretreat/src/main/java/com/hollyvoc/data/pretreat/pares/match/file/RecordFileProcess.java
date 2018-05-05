package com.hollyvoc.data.pretreat.pares.match.file;

import com.hollyvoc.data.pretreat.config.Const;
import com.hollyvoc.data.pretreat.pares.match.meta.RecordingMeta;
import lombok.extern.log4j.Log4j;

import java.util.Map;

import static com.hollyvoc.data.pretreat.config.Const.CodeType.USE_CONTACT;


/**
 * Created by zhaihw on 2017/2/9.
 *
 */
@Log4j
public class RecordFileProcess extends MapStructuredFileProcess<RecordingMeta> {

    @Override
    protected void assign(String line, String prov, Map<Const.CodeType, Map<String, String>> maps, Map<String, Object> result) {
        String[] lines = line.split("\\|");
        if( lines.length == 13) {
            RecordingMeta meta = new RecordingMeta();
            int i = 0;
            meta.setRecordNo(lines[i++]);
            meta.setCallId(lines[i++]);
            meta.setRecordStartTime(lines[i++]);
            meta.setRecordEndTime(lines[i++]);
            meta.setRecordLength(lines[i++]);
            meta.setRecordPath(lines[i++]);
            meta.setRecordFileName(lines[i++]);
            meta.setRecordFormat(lines[i++]);
            meta.setRecordSampRate(lines[i++]);
            meta.setRecordEncodeRate(lines[i++]);
            meta.setCaller(lines[i++]);
            meta.setCallee(lines[i++]);
            meta.setContactId(lines[i]);
            Map<String, String> useMap = maps.get(USE_CONTACT); // 使用哪个字段作为主键
            if(useMap == null || useMap.size() == 0 || !useMap.containsKey(prov)) {
                result.put(ID, meta.getCallId());
            } else {
                result.put(ID, meta.getContactId());
            }
            result.put(OBJ, meta);
        } else {
            log.warn("record ERR - " + prov + ":" + line);
        }
    }
}
