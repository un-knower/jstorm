package com.hollyvoc.data.pretreat.pares.match.file;

import com.hollyvoc.data.pretreat.config.Const;
import com.hollyvoc.data.pretreat.pares.match.meta.SerRequestMeta;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

import static com.hollyvoc.data.pretreat.config.Const.CODE_VALUE_ERR;
import static com.hollyvoc.data.pretreat.config.Const.CODE_VALUE_NAN;
import static com.hollyvoc.data.pretreat.config.Const.CodeType.BUSINESS_TYPE;
import static com.hollyvoc.data.pretreat.config.Const.CodeType.SHEET_TYPE;


/**
 * Created by zhaihw on 2017/2/9.
 *
 */
@Log4j
public class SerRequestFileProcess extends MapStructuredFileProcess<SerRequestMeta> {

    @Override
    protected void assign(String line, String prov, Map<Const.CodeType, Map<String, String>> maps, Map<String, Object> result) {
        String[] lines = line.split("\\|");
        if(lines.length == 6) {
            SerRequestMeta meta = new SerRequestMeta();
            int i = 0;
            meta.setSerRequestId(lines[i++]);
            meta.setBusinessType(checkCode(lines[i++], maps.get(BUSINESS_TYPE)));
            meta.setServiceType(lines[i++]);
            meta.setSheetNo(lines[i++]);
            meta.setSheetType(checkCode(lines[i++], maps.get(SHEET_TYPE)));
            meta.setContactId(lines[i]);
            result.put(ID, meta.getContactId());
            result.put(OBJ, meta);
        }else{
            log.warn("ser-request ERR " +  prov + " :" + line);
        }
    }

    /**
     * 编码校验
     * @param val 文件中原始编码
     * @param maps 编码集合
     * @return 校验后结果
     */
    private String checkCode(final String val, Map<String, String> maps) {
        if(StringUtils.isEmpty(val)) return CODE_VALUE_NAN;
        String name = maps.get(val);
        if(StringUtils.isEmpty(name)) {
            return CODE_VALUE_ERR;
        }
        return val;
    }
}
