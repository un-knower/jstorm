package com.hollyvoc.data.pretreat.pares.service;

import com.hollyvoc.data.pretreat.bean.TblVocCustcontinfo;
import com.hollyvoc.data.pretreat.config.Config;
import com.hollyvoc.data.pretreat.config.Const;
import com.hollyvoc.data.pretreat.jdbc.JdbcUtil;
import com.hollyvoc.data.pretreat.pares.match.DataMatcher;
import com.hollyvoc.data.pretreat.pares.voice.bean.ParseResult;
import com.hollyvoc.data.pretreat.pares.voice.parsing.IVoiceParse;
import com.hollyvoc.data.pretreat.util.GzipUtils;
import lombok.extern.log4j.Log4j;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hollyvoc.data.pretreat.config.Const.*;
import static com.hollyvoc.data.pretreat.config.Const.CodeType.*;
import static com.hollyvoc.data.pretreat.pares.voice.parsing.factory.VoiceParserFactory.getInstance;

/**
 * Created by qianxm on 2018/1/9.
 */
@Log4j
public class paresData {




    public void parse(String txtLocal, String xmlLocal, String provDir, String prov, String day, String hour, String recordLocal, String controlFile) throws Exception {

        Map<String, Map<String, String>> codeGroups = getCode(CT_AREA_CODE, CT_AREA_TREE, CT_BUSINESS_TYPE,
                CT_CUST_BRAND, CT_CUST_LEVEL, CT_DIRECTION, CT_NET_TYPE, CT_SATISFACTION, CT_SHEET_TYPE);
        // 1. 读取数据库编码
        Map<Const.CodeType, Map<String, String>> maps = new HashMap<>();
        maps.put(NET_TYPE, codeGroups.get(CT_NET_TYPE));
        maps.put(BUSINESS_TYPE, codeGroups.get(CT_BUSINESS_TYPE));
        maps.put(CUST_BAND, codeGroups.get(CT_CUST_BRAND));
        maps.put(CUST_LEVEL, codeGroups.get(CT_CUST_LEVEL));
        maps.put(AREA_TREE, codeGroups.get(CT_AREA_TREE));
        maps.put(SATISFACTION, codeGroups.get(CT_SATISFACTION));
        maps.put(Const.CodeType.AREA_CODE, codeGroups.get(CT_AREA_CODE));
        maps.put(SHEET_TYPE, codeGroups.get(CT_SHEET_TYPE));
        maps.put(USE_CONTACT, codeGroups.get(CT_USE_CONTACT));
        // 2. 解析txt文件
        List<TblVocCustcontinfo> info = parseTxt(txtLocal, day, hour, prov, maps);
        if (info.size() == 0) {
            return;
        }
//        Map<String, TblVocCustcontinfo> singleMaps = new HashMap<>();
//        info.forEach(voc -> singleMaps.put(voc.getId(), voc));
//        info.clear();
//        singleMaps.forEach((k, v) -> info.add(v));

//        log.info("PMJ - before commit info size :" + info.size());
        File f = new File(xmlLocal);
        int count = f.listFiles().length;
//        log.info("PMJ- local xml count: " + count);
        // 3. 根据结构化信息解析xml 并提交至hbase 返回具有XML的对象，用于后续处理
        info = parseXml(day, hour, info, xmlLocal, provDir, maps, controlFile);

    }

    private List<TblVocCustcontinfo> parseXml(String day, String hour, List<TblVocCustcontinfo> info, String xmlLocal, String provDir,
                                              Map<CodeType, Map<String, String>> maps, String controlFile) throws Exception{
        FileChannel oriFc = null, sfc = null;
        try {
            // 写文件之前根据控制文件名称先判断该时间点的内容是否已经写入
//            if(controlDao.get(controlFile) == 0){
//                oriFc = FileUtils.getWriterFileChannel(provDir + separator + getFileName(day, Original, null), true); //用于记录实际文本内容
//                sfc = FileUtils.getWriterFileChannel(provDir + separator + getFileName(day, Split, null), true); //用于记录分词文本内容
//                // 本地文件写入成功后，就将成功的控制文件名称保存
//                controlDao.inset(controlFile);
//            }

        }catch (Exception e) {
            log.error("PMJ - error, getWriterFileChannel err ", e);
        }
        return parseXml(day + hour, info, xmlLocal, oriFc, sfc, maps);
    }

    private List<TblVocCustcontinfo> parseTxt(String txtLocal, String day, String hour, String prov,
                                              Map<Const.CodeType, Map<String, String>> maps) throws Exception {
        File ltf = new File(txtLocal);
        String[] tfs = ltf.list();
        String record = null, contact = null, request = null;
        for(String fileName : tfs) {
            if (!fileName.contains(day + hour)) continue; // 不包含日期的文件属于无效文件，忽略即可
            String local = txtLocal + fileName;
            if (fileName.contains(FN_RECOINFO)) {
                record  = local;
            } else if (fileName.contains(FN_CUSTCONTINFO)) {
                contact = GzipUtils.doUncompressFile(local); // 解压
            } else if (fileName.contains(FN_SEREQUEST)) {
                request = GzipUtils.doUncompressFile(local); // 解压
            }
        }
        if(record == null || contact == null )
            throw new Exception("file not found,record:" + record +",contact" + contact );
        DataMatcher dm = new DataMatcher(contact, request, record, prov, day, hour, maps);
        dm.init();
        return dm.matcher();
    }


    private List<TblVocCustcontinfo> parseXml(String hours, List<TblVocCustcontinfo> info, String xmlLocal, FileChannel oriFc, FileChannel sfc,
                                              Map<Const.CodeType, Map<String, String>> maps) throws Exception{
        File file = new File(xmlLocal);
        String[] files = file.list();
        int size = files == null ? 0 :files.length;
        if(size == 0) {
            throw new Exception("no xml " + xmlLocal);
        }
        List<TblVocCustcontinfo> newInfo = new ArrayList<>(size);
        int i = 0;
        for (TblVocCustcontinfo voc : info){
            try {
                String xmlPath = xmlLocal + voc.getRecordName().substring(0, voc.getRecordName().lastIndexOf(".")) + ".xml";
                File xmlFile = new File(xmlPath);
                if (!xmlFile.exists()) {
//                    log.info("recordName: " + voc.getRecordName() + " xml: " + xmlPath);
                    continue;
                }
                IVoiceParse xmlParser = getInstance().pointParser(Config.getVal("voiceParser"), new FileInputStream(xmlFile), false);
                ParseResult pr = xmlParser.parse();
                voc.setRecoinfoLength(pr.getDuration());
                voc.setSilenceLength(pr.getSilence());
                voc.setTxtContent(pr.getAllText());
                voc.setTxtContentAgent(pr.getAgentText());
                voc.setTxtContentUser(pr.getUserText());
                newInfo.add(voc);
            }catch (Exception e) {
                log.error("PMJ - parseXml", e);
            }
        }
        return newInfo;
    }


    public static Map<String,Map<String,String>> getCode(String ... types) throws Exception{
        Map<String,Map<String,String>> codeGroups = new HashMap<>();
        String sql;
        for(String type : types) {
            if(type.equals(CT_AREA_TREE)) {
                sql = "select code_value, code_name from tbl_sys_tree_code where code_type = ?";
            }else{
                sql = "select g.code_value, g.code_name from tbl_sys_general_code g where g.enabled='1' and g.code_type=?";
            }
            Map<String,String> codeValues = JdbcUtil.getInstance().query(sql, new Object[]{type}, rs -> {
                Map<String,String> cvMap = new HashMap<String, String>();
                while (rs.next()) {
                    cvMap.put(rs.getString("code_name"), rs.getString("code_value"));
                }
                return cvMap;
            });
            codeGroups.put(type, codeValues);
        }
        return codeGroups;
    }
}
