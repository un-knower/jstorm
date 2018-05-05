package com.hollyvoc.data.pretreat.bean;


import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class TblVocCustcontinfo {

	public static Map<String, String> mapTable = new HashMap<>();
	public static  Class<TblVocCustcontinfo> clazz = TblVocCustcontinfo.class;

	static {

		//定义需要从hbase获取的列族和列
		mapTable.put("custcontinfoId","common");
		mapTable.put("recoinfoLength","common");
		mapTable.put("silenceLength","common");
		mapTable.put("userCode","common");
		mapTable.put("caller","common");
		mapTable.put("callee","common");
		mapTable.put("areaCode","common");
		mapTable.put("custArea","common");
		mapTable.put("businessType","common");
		mapTable.put("custBand","common");
		mapTable.put("custLevel","common");
		mapTable.put("satisfication","common");
		mapTable.put("acceptTime","common");
		mapTable.put("recordName","common");
		mapTable.put("recordFile","common");
		mapTable.put("txtContent","content");
	}

	// Fields
	//分为三个列族:common、content、other
	//colFamily:  common
	private String custcontinfoId;
	private Long recoinfoLength;//通话时长
	private Long silenceLength;//静音时长
	//private String silenceLengthRange;//静音占比
	private String userCode;//坐席工号
	private String caller;//主叫号码
	private String callee;//被叫号码
	private String areaCode; // 省份
	private String custArea;//地市
	private String businessType;//业务类型
	private String custBand;//用户品牌
	private String custLevel;//用户等级
	private String satisfication;//满意度
	private String acceptTime;//受理时间
	private String recordName;
	private String recordFile;
	private String diretion; //呼叫方向


	//colFamily:  content

	private String txtContent;
	private String txtContentUser;
	private String txtContentAgent;


	//colFamily:  other
	private String mobileNo;
	private String netType;
	private String startTime;
	private String year;
	private String month;
	private String week;
	private String day;
	private String serviceType;
	private String recordLengthRange;
	private String silenceLengthRange;
	private String sheetType;
	private String sheetNo;
	private String queue ;
	private String talkingStartTime;
	private String talkingEndTime;
	private String queueStartTime;
	private String queueEndTime;
	private String answerStartTime;
	private String answerEndTime;
	private String callId;


	// --------------

	private String serviceTypeL1;
	private String serviceTypeL2;
	private String serviceTypeL3;
	private String domainId;
	private String operateTime;
	private String isVoerlength;
	private Long talkingLength;
	private Long marketVoiceLen;
	private Long customerVoiceLen;
	private Long marketSilenceLen;
	private String marketVoiceSpeed;
	private String customerVoiceSpeed;
	private Long customerSilenceLen;
	private String recordTransPath;
	private String recordTransName;


	private String recordFormat;
	private String recordSampRate;
	private String recordEncodeRate;

//	@Column(name="record_format")
	public String getRecordFormat() {
		return recordFormat;
	}

	public void setRecordFormat(String recordFormat) {
		this.recordFormat = recordFormat;
	}
//	@Column(name="record_samp_rate")
	public String getRecordSampRate() {
		return recordSampRate;
	}

	public void setRecordSampRate(String recordSampRate) {
		this.recordSampRate = recordSampRate;
	}
//	@Column(name="record_encode_rate")
	public String getRecordEncodeRate() {
		return recordEncodeRate;
	}

	public void setRecordEncodeRate(String recordEncodeRate) {
		this.recordEncodeRate = recordEncodeRate;
	}

	/**
	 * 静音时长占比
	 */
	private String silenceRate;
	/**
	 * 行索引
	 */
	private String rowIndex;
	/**
	 * 匹配到的模型列表
	 */
	private List<String> modelNames;

	private String conversation;
	private String id;

	// Constructors
	/** default constructor */
	public TblVocCustcontinfo() {
	}

	/** minimal constructor */
	public TblVocCustcontinfo(String custcontinfoId) {
		this.custcontinfoId = custcontinfoId;
	}

	/** full constructor */
	public TblVocCustcontinfo(String custcontinfoId, String callId,
                              String areaCode, String userCode, String caller, String callee,
                              String mobileNo, String acceptTime, String year, String month,
                              String week, String day, String custArea, String custBand,
                              String startTime, Long recoinfoLength, Long silenceLength,
                              String queue, String queueStartTime, String queueEndTime,
                              String answerStartTime, String answerEndTime,
                              String talkingStartTime, String talkingEndTime,
                              String satisfication, String recordFile, String txtContent,
                              String txtContentUser, String txtContentAgent, String businessType,
                              String serviceType, String serviceTypeL1, String serviceTypeL2,
                              String serviceTypeL3, String domainId, String operateTime,
                              String isVoerlength, String custLevel, Long talkingLength,
                              Long marketVoiceLen, Long customerVoiceLen, Long marketSilenceLen,
                              String marketVoiceSpeed, String customerVoiceSpeed,
                              Long customerSilenceLen) {
		this.custcontinfoId = custcontinfoId;
		this.callId = callId;
		this.areaCode = areaCode;
		this.userCode = userCode;
		this.caller = caller;
		this.callee = callee;
		this.mobileNo = mobileNo;
		this.acceptTime = acceptTime;
		this.year = year;
		this.month = month;
		this.week = week;
		this.day = day;
		this.custArea = custArea;
		this.custBand = custBand;
		this.startTime = startTime;
		this.recoinfoLength = recoinfoLength;
		this.silenceLength = silenceLength;
		this.queue = queue;
		this.queueStartTime = queueStartTime;
		this.queueEndTime = queueEndTime;
		this.answerStartTime = answerStartTime;
		this.answerEndTime = answerEndTime;
		this.talkingStartTime = talkingStartTime;
		this.talkingEndTime = talkingEndTime;
		this.satisfication = satisfication;
		this.recordFile = recordFile;
		this.txtContent = txtContent;
		this.txtContentUser = txtContentUser;
		this.txtContentAgent = txtContentAgent;
		this.businessType = businessType;
		this.serviceType = serviceType;
		this.serviceTypeL1 = serviceTypeL1;
		this.serviceTypeL2 = serviceTypeL2;
		this.serviceTypeL3 = serviceTypeL3;
		this.domainId = domainId;
		this.operateTime = operateTime;
		this.isVoerlength = isVoerlength;
		this.custLevel = custLevel;
		this.talkingLength = talkingLength;
		this.marketVoiceLen = marketVoiceLen;
		this.customerVoiceLen = customerVoiceLen;
		this.marketSilenceLen = marketSilenceLen;
		this.marketVoiceSpeed = marketVoiceSpeed;
		this.customerVoiceSpeed = customerVoiceSpeed;
		this.customerSilenceLen = customerSilenceLen;
	}
//	@Column(name = "record_Name")
	public String getRecordName() {
		return recordName;
	}
//	@Transient
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

//	@Column(name="sheet_TYpe")
	public String getSheetType() {
		return sheetType;
	}

	public void setSheetType(String sheetType) {
		this.sheetType = sheetType;
	}

//	@Column(name="sheet_no")
	public String getSheetNo() {
		return sheetNo;
	}

	public void setSheetNo(String sheetNo) {
		this.sheetNo = sheetNo;
	}

//	@Column(name="net_type")
	public String getNetType() {
		return netType;
	}
//	@Column(name="record_trans_path")
	public String getRecordTransPath() {
		return recordTransPath;
	}
//	@Column(name="record_trans_name")
	public String getRecordTransName() {
		return recordTransName;
	}

	public void setRecordTransName(String recordTransName) {
		this.recordTransName = recordTransName;
	}

	public void setRecordTransPath(String recordTransPath) {
		this.recordTransPath = recordTransPath;
	}

	public void setNetType(String netType) {
		this.netType = netType;
	}

	// Property accessors
//	@Id
//	@Column(name = "CUSTCONTINFO_ID", unique = true, nullable = false, length = 40)
	public String getCustcontinfoId() {
		return this.custcontinfoId;
	}

	public void setCustcontinfoId(String custcontinfoId) {
		this.custcontinfoId = custcontinfoId;
	}

//	@Column(name = "CALL_ID", length = 40)
	public String getCallId() {
		return this.callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

//	@Column(name = "AREA_CODE", length = 10)
	public String getAreaCode() {
		return this.areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

//	@Column(name = "USER_CODE", length = 20)
	public String getUserCode() {
		return this.userCode;
	}

	public void setUserCode(String userCode) {
		this.userCode = userCode;
	}

//	@Column(name = "CALLER", length = 200)
	public String getCaller() {
		return this.caller;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

//	@Column(name = "CALLEE", length = 200)
	public String getCallee() {
		return this.callee;
	}

	public void setCallee(String callee) {
		this.callee = callee;
	}

//	@Column(name = "MOBILE_NO", length = 200)
	public String getMobileNo() {
		return this.mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

//	@Column(name = "ACCEPT_TIME", length = 20)
	public String getAcceptTime() {
		return this.acceptTime;
	}

	public void setAcceptTime(String acceptTime) {
		this.acceptTime = acceptTime;
	}

//	@Column(name = "YEAR", length = 40)
	public String getYear() {
		return this.year;
	}

	public void setYear(String year) {
		this.year = year;
	}

//	@Column(name = "MONTH", length = 40)
	public String getMonth() {
		return this.month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

//	@Column(name = "WEEK", length = 40)
	public String getWeek() {
		return this.week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

//	@Column(name = "DAY", length = 40)
	public String getDay() {
		return this.day;
	}

	public void setDay(String day) {
		this.day = day;
	}

//	@Column(name = "CUST_AREA", length = 20)
	public String getCustArea() {
		return this.custArea;
	}

	public void setCustArea(String custArea) {
		this.custArea = custArea;
	}
//
//	@Column(name = "CUST_BAND", length = 20)
	public String getCustBand() {
		return this.custBand;
	}

	public void setCustBand(String custBand) {
		this.custBand = custBand;
	}

//	@Column(name = "START_TIME", length = 20)
	public String getStartTime() {
		return this.startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

//	@Column(name = "RECOINFO_LENGTH", precision = 12, scale = 0)
	public Long getRecoinfoLength() {
		return ((Double)Math.ceil(this.recoinfoLength / (double)1000)).longValue();
	}

	public void setRecoinfoLength(Long recoinfoLength) {
		this.recoinfoLength = recoinfoLength;
	}

//	@Column(name = "SILENCE_LENGTH", precision = 10, scale = 0)
	public Long getSilenceLength() {
		return ((Double)Math.ceil(this.silenceLength / (double)1000)).longValue();
	}

	public void setSilenceLength(Long silenceLength) {
		this.silenceLength = silenceLength;
	}

//	@Column(name = "QUEUE", length = 50)
	public String getQueue() {
		return this.queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

//	@Column(name = "QUEUE_START_TIME", length = 20)
	public String getQueueStartTime() {
		return this.queueStartTime;
	}

	public void setQueueStartTime(String queueStartTime) {
		this.queueStartTime = queueStartTime;
	}

//	@Column(name = "QUEUE_END_TIME", length = 20)
	public String getQueueEndTime() {
		return this.queueEndTime;
	}

	public void setQueueEndTime(String queueEndTime) {
		this.queueEndTime = queueEndTime;
	}

//	@Column(name = "ANSWER_START_TIME", length = 20)
	public String getAnswerStartTime() {
		return this.answerStartTime;
	}

	public void setAnswerStartTime(String answerStartTime) {
		this.answerStartTime = answerStartTime;
	}

//	@Column(name = "ANSWER_END_TIME", length = 20)
	public String getAnswerEndTime() {
		return this.answerEndTime;
	}

	public void setAnswerEndTime(String answerEndTime) {
		this.answerEndTime = answerEndTime;
	}

//	@Column(name = "TALKING_START_TIME", length = 20)
	public String getTalkingStartTime() {
		return this.talkingStartTime;
	}

	public void setTalkingStartTime(String talkingStartTime) {
		this.talkingStartTime = talkingStartTime;
	}

//	@Column(name = "TALKING_END_TIME", length = 20)
	public String getTalkingEndTime() {
		return this.talkingEndTime;
	}

	public void setTalkingEndTime(String talkingEndTime) {
		this.talkingEndTime = talkingEndTime;
	}

//	@Column(name = "SATISFICATION", length = 2)
	public String getSatisfication() {
		return this.satisfication;
	}

	public void setSatisfication(String satisfication) {
		this.satisfication = satisfication;
	}

//	@Column(name = "RECORD_FILE", length = 500)
	public String getRecordFile() {
		return this.recordFile;
	}

	public void setRecordFile(String recordFile) {
		this.recordFile = recordFile;
	}

//	@Column(name = "TXT_CONTENT", length = 2000)
	public String getTxtContent() {
		return this.txtContent;
	}

	public void setTxtContent(String txtContent) {
		this.txtContent = txtContent;
	}

//	@Column(name = "TXT_CONTENT_USER", length = 2000)
	public String getTxtContentUser() {
		return this.txtContentUser;
	}

	public void setTxtContentUser(String txtContentUser) {
		this.txtContentUser = txtContentUser;
	}

//	@Column(name = "TXT_CONTENT_AGENT", length = 2000)
	public String getTxtContentAgent() {
		return this.txtContentAgent;
	}

	public void setTxtContentAgent(String txtContentAgent) {
		this.txtContentAgent = txtContentAgent;
	}

//	@Column(name = "BUSINESS_TYPE", length = 8)
	public String getBusinessType() {
		return this.businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

//	@Column(name = "SERVICE_TYPE", length = 512)
	public String getServiceType() {
		return this.serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

//	@Column(name = "SERVICE_TYPE_L1", length = 512)
	public String getServiceTypeL1() {
		return this.serviceTypeL1;
	}

	public void setServiceTypeL1(String serviceTypeL1) {
		this.serviceTypeL1 = serviceTypeL1;
	}

//	@Column(name = "SERVICE_TYPE_L2", length = 512)
	public String getServiceTypeL2() {
		return this.serviceTypeL2;
	}

	public void setServiceTypeL2(String serviceTypeL2) {
		this.serviceTypeL2 = serviceTypeL2;
	}

//	@Column(name = "SERVICE_TYPE_L3", length = 512)
	public String getServiceTypeL3() {
		return this.serviceTypeL3;
	}

	public void setServiceTypeL3(String serviceTypeL3) {
		this.serviceTypeL3 = serviceTypeL3;
	}

//	@Column(name = "DOMAIN_ID", length = 4)
	public String getDomainId() {
		return this.domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

//	@Column(name = "OPERATE_TIME", length = 20)
	public String getOperateTime() {
		return this.operateTime;
	}

	public void setOperateTime(String operateTime) {
		this.operateTime = operateTime;
	}

//	@Column(name = "IS_VOERLENGTH", length = 10)
	public String getIsVoerlength() {
		return this.isVoerlength;
	}

	public void setIsVoerlength(String isVoerlength) {
		this.isVoerlength = isVoerlength;
	}

//	@Column(name = "CUST_LEVEL", length = 20)
	public String getCustLevel() {
		return this.custLevel;
	}

	public void setCustLevel(String custLevel) {
		this.custLevel = custLevel;
	}

//	@Column(name = "TALKING_LENGTH", precision = 10, scale = 0)
	public Long getTalkingLength() {
		return this.talkingLength;
	}

	public void setTalkingLength(Long talkingLength) {
		this.talkingLength = talkingLength;
	}

//	@Column(name = "MARKET_VOICE_LEN", precision = 10, scale = 0)
	public Long getMarketVoiceLen() {
		return this.marketVoiceLen;
	}

	public void setMarketVoiceLen(Long marketVoiceLen) {
		this.marketVoiceLen = marketVoiceLen;
	}

//	@Column(name = "CUSTOMER_VOICE_LEN", precision = 10, scale = 0)
	public Long getCustomerVoiceLen() {
		return this.customerVoiceLen;
	}

	public void setCustomerVoiceLen(Long customerVoiceLen) {
		this.customerVoiceLen = customerVoiceLen;
	}

//	@Column(name = "MARKET_SILENCE_LEN", precision = 10, scale = 0)
	public Long getMarketSilenceLen() {
		return this.marketSilenceLen;
	}

	public void setMarketSilenceLen(Long marketSilenceLen) {
		this.marketSilenceLen = marketSilenceLen;
	}

//	@Column(name = "MARKET_VOICE_SPEED", length = 40)
	public String getMarketVoiceSpeed() {
		return this.marketVoiceSpeed;
	}

	public void setMarketVoiceSpeed(String marketVoiceSpeed) {
		this.marketVoiceSpeed = marketVoiceSpeed;
	}

//	@Column(name = "CUSTOMER_VOICE_SPEED", length = 40)
	public String getCustomerVoiceSpeed() {
		return this.customerVoiceSpeed;
	}

	public void setCustomerVoiceSpeed(String customerVoiceSpeed) {
		this.customerVoiceSpeed = customerVoiceSpeed;
	}

//	@Column(name = "CUSTOMER_SILENCE_LEN", precision = 10, scale = 0)
	public Long getCustomerSilenceLen() {
		return this.customerSilenceLen;
	}

	public void setCustomerSilenceLen(Long customerSilenceLen) {
		this.customerSilenceLen = customerSilenceLen;
	}

	public String getRecordLengthRange() {
		return recordLengthRange;
	}

	public void setRecordLengthRange(String recordLengthRange) {
		this.recordLengthRange = recordLengthRange;
	}

	public String getSilenceLengthRange() {
		return silenceLengthRange;
	}

	public void setSilenceLengthRange(String silenceLengthRange) {
		this.silenceLengthRange = silenceLengthRange;
	}

	public String getSilenceRate() {
		if(silenceLength == null)
			return "未知";
		double d = silenceLength * 100.0 / recoinfoLength;
		silenceRate = String.format("%.2f", d) + "%";
		return silenceRate;
	}

	public String getDiretion() {
		return diretion;
	}

	public void setDiretion(String diretion) {
		this.diretion = diretion;
	}

	public void setSilenceRate(String silenceRate) {
		this.silenceRate = silenceRate;
	}

	public String getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(String rowIndex) {
		this.rowIndex = rowIndex;
	}

//	@Transient
	public List<String> getModelNames() {
		return modelNames;
	}

	public void setModelNames(List<String> modelNames) {
		this.modelNames = modelNames;
	}
//	@Transient
	public String getConversation() {
		String c = this.txtContent;
		if (!StringUtils.isEmpty(c)) {
			c = c.replaceAll("n1#", "<br/>客户：");
			c = c.replaceAll("n0#", "<br/>坐席：");
			conversation= c;
		}else {
			c = "无内容";
		}
		return conversation;
	}

	@Override
	public String toString() {
		return this.custcontinfoId
		+ "|" + areaCode
		+ "|" + userCode
		+ "|" + caller
		+ "|" + callee
		+ "|" + mobileNo
		+ "|" + acceptTime
		+ "|" + year
		+ "|" + month
		+ "|" + week
		+ "|" + day
		+ "|" + custArea
		+ "|" + this.custBand
		+ "|" + satisfication
		+ "|" + queue
		+ "|" + serviceType
		+ "|" + sheetNo //无需索引
		+ "|" + sheetType
		+ "|" + netType
		+ "|" + recordName //无需索引
		+ "|" + businessType
		+ "|" + custLevel
		+ "|" + this.diretion
		+ "|" + recordEncodeRate
		+ "|" + recordSampRate
		+ "|" + recordFormat
		+ "|" + silenceLength
		+ "|" + this.recoinfoLength;
	}
}