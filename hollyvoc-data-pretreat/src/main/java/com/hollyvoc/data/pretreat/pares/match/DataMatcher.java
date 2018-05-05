package com.hollyvoc.data.pretreat.pares.match;


import com.hollyvoc.data.pretreat.bean.TblVocCustcontinfo;
import com.hollyvoc.data.pretreat.config.Const;
import com.hollyvoc.data.pretreat.pares.match.file.*;
import com.hollyvoc.data.pretreat.pares.match.meta.ContactMeta;
import com.hollyvoc.data.pretreat.pares.match.meta.RecordingMeta;
import com.hollyvoc.data.pretreat.pares.match.meta.SerRequestMeta;
import com.hollyvoc.data.pretreat.util.DateUtils;
import com.hollyvoc.data.pretreat.util.MD5Util;
import lombok.extern.log4j.Log4j;

import java.util.*;
import java.util.regex.Pattern;

import static com.hollyvoc.data.pretreat.config.Const.*;
import static com.hollyvoc.data.pretreat.config.Const.CodeType.USE_CONTACT;
import static org.apache.commons.lang.StringUtils.reverse;

/**
 * Created by zhaihw on 2017/2/10.
 *  多线程解析
 */
@Log4j
public class DataMatcher {


    private Map<Const.CodeType, Map<String, String>> codeMaps;
    private String contactPath;
    private String requestPath;
    private String recordPath;
    private String prov;
    private String day;
    private String hour;
    // 解析文件的线程
    private List<ContactMeta> contacts = new ArrayList<>();
    private Map<String, RecordingMeta> records = new HashMap<>();
    private Map<String, SerRequestMeta> requests = new HashMap<>();
    // 各个线程
    private Thread contactThread, recordThread, requestThread;

    public DataMatcher(String contactPath, String requestPath, String recordPath, String prov, String day, String hour,
                       Map<Const.CodeType, Map<String, String>> maps) {
        this.codeMaps = maps;
        this.contactPath = contactPath;
        this.requestPath = requestPath;
        this.recordPath = recordPath;
        this.prov = prov;
        this.day = day;
        this.hour = hour;
    }

    /**
     * 初始化
     */
    public void init() {
        contactThread = new Thread(() -> {
            ListStructuredFileProcess<ContactMeta> process = new ContactFileProcess();
            try {
                contacts.addAll(process.parse(contactPath, codeMaps));
            }catch (Exception e) {
                log.error("PMJ - parse contact file err:" + prov +"-" + day + "-" + hour, e);
            }
        }, "contactThread");
        recordThread = new Thread(() -> {
            MapStructuredFileProcess<RecordingMeta> process = new RecordFileProcess();
            try {
                records.putAll(process.parse(recordPath, prov, codeMaps));
            }catch (Exception e) {
                log.error("PMJ - parse records file err:" + prov +"-" + day + "-" + hour, e);
            }
        }, "recordThread");
        requestThread = new Thread(() -> {
            if(requestPath == null) return;
            MapStructuredFileProcess<SerRequestMeta> process = new SerRequestFileProcess();
            try {
                requests.putAll(process.parse(requestPath, prov, codeMaps));
            }catch (Exception e) {
                log.error("PMJ - parse records file err:" + prov +"-" + day + "-" + hour, e);
            }
        }, "requestThread");
    }

    /**
     * 线程启动
     */
    private void start() {
        contactThread.start();
        recordThread.start();
        requestThread.start();
    }

    /**
     * 线程合并
     */
    private void join() {
        try {
            contactThread.join();
        }catch (InterruptedException e) {
            log.error("PMJ - contactThread join", e);
        }
        try{
            recordThread.join();
        }catch (InterruptedException e) {
            log.error("PMJ - recordThread join", e);
        }
        try {
            requestThread.join();
        }catch (InterruptedException e) {
            log.error("PMJ - requestThread join", e);
        }
    }

    /**
     * 数据匹配
     * @return 匹配结果
     */
    public List<TblVocCustcontinfo> matcher() {
        this.start(); // 多线程解析文件
        this.join(); // 等待线程解析结束

//        Monitor.getInstance().getMonitorData().setRealCustcontinfoCount(contacts.size());
//        Monitor.getInstance().getMonitorData().setRealRecoinfoCount(records.size());
//        Monitor.getInstance().getMonitorData().setRealSerequesCount(requests.size());

        List<TblVocCustcontinfo> info = new ArrayList<>(contacts.size());
        Map<String, String> useMap = this.codeMaps.get(USE_CONTACT); // 使用哪个字段作为主键
        String rowKeyPrefix = reverse(day + hour) + prov;
        boolean useContact = useMap == null || useMap.size() == 0 || !useMap.containsKey(prov);
        if(contacts != null)
            contacts.forEach(cm -> {
                String id = null;
                try {
                    id = useContact ?  cm.getCallId() : cm.getContactId();
                    RecordingMeta rm = records.get(id);
                    SerRequestMeta srm = requests.get(cm.getContactId());
                    info.add(assign(cm, rm, srm, rowKeyPrefix, useContact));
                }catch (Exception e) {
//                    log.error("PMJ - id=" + id + " useContact=" + useContact, e);
                }
            });
//        Monitor.getInstance().getMonitorData().setContactMatchCount(info.size());
        return info;
    }

    private TblVocCustcontinfo assign(ContactMeta cm, RecordingMeta rm, SerRequestMeta srm, String rowKeyPrefix,
                                      boolean useContact) {
        String id = useContact ? cm.getCallId() : cm.getContactId();
        TblVocCustcontinfo voc = new TblVocCustcontinfo();
        voc.setId(rowKeyPrefix + Math.abs(MD5Util.encrypt_string(rowKeyPrefix + id, "UTF-8").hashCode()));
        voc.setCallId(cm.getCallId());
        voc.setCustcontinfoId(cm.getContactId());
        voc.setAreaCode(prov);
        voc.setUserCode(cm.getAgentCode());
        voc.setCallee(cm.getCallee());
        voc.setCaller(cm.getCaller());
        voc.setMobileNo(cm.getMobileNo());
        voc.setAcceptTime(cm.getAcceptTime());
        voc.setYear(day.substring(0, 4));
        voc.setMonth(day.substring(0, 6));
        voc.setDay(day);
        try {
            Date cur = DateUtils.parseDate(day, DateUtils.DF_YYYYMMDD);
            Calendar cal = Calendar.getInstance();
            cal.setTime(cur);
            voc.setWeek(cal.get(Calendar.WEEK_OF_YEAR) + "");
        }catch (Exception e) {
//            log.error("PML - get getdate err", e);
        }
        voc.setCustArea(cm.getCustArea());
        voc.setCustBand(cm.getCustBand());
        voc.setCustLevel(cm.getCustLevel());
        voc.setSatisfication(cm.getSatisfaction());
        voc.setQueue(cm.getQueue());
        voc.setDiretion(cm.getDirection());
        if(srm != null) {
            voc.setServiceType(srm.getServiceType());
            voc.setBusinessType(srm.getBusinessType());
            voc.setSheetNo(srm.getSheetNo());
            voc.setSheetType(srm.getSheetType());
        }else{
            voc.setServiceType(CODE_VALUE_NAN);
            voc.setBusinessType(CODE_VALUE_NAN);
        }
        voc.setRecordName(rm.getRecordFileName());
        voc.setRecordEncodeRate(rm.getRecordEncodeRate());
        voc.setRecordSampRate(rm.getRecordSampRate());
        voc.setRecordFormat(rm.getRecordFormat());

        voc.setNetType(getNetType(voc.getMobileNo()));
        return voc;
    }

    private String getNetType(String mobileNo) {
        if(Pattern.compile(REGEX_UNICOM).matcher(mobileNo).find())
            return NET_TYPE_UNICON;
        if(Pattern.compile(REGEX_MVNO_UNICOM).matcher(mobileNo).find())
            return NET_TYPE_MVNO_UNKNOW;
        if(Pattern.compile(REGEX_MOBILE).matcher(mobileNo).find())
            return NET_TYPE_MOBILE;
        if(Pattern.compile(REGEX_MVNO_MOBILE).matcher(mobileNo).find())
            return NET_TYPE_MVNO_MOBILE;
        if(Pattern.compile(REGEX_TELECOM).matcher(mobileNo).find())
            return NET_TYPE_TELECOM;
        if(Pattern.compile(REGEX_MVNO_TELECOM).matcher(mobileNo).find())
            return NET_TYPE_MVNO_TELECOM;
        return NET_TYPE_MVNO_UNKNOW;
    }
}
