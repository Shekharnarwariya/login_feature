package com.hti.smpp.common.request;

public class SmscEntryRequest {

	private String name;
	private String password;
	private String ip;
	private String backupIp;
	private int port;
	private String bindMode;
	private int sleep;
	private boolean bind;
	private boolean replaceSource; // replace senderId if not found in allowed list
	private String defaultSource; // default senderId to be replaced
	private String allowedSources; // comma separated allowed senderId list
	private boolean destRestrict; // Restrict same destination under specific duration
	private int minDestTime; // duration between 2 duplicate destination
	private boolean looping; // duplicate packet(senderId + destination + content) restrictions
	private int loopCount; // number of duplicate packets
	private int loopDuration; // duration for duplicate count
	private boolean hexResponse; // hexadecimal Submit response on submit
	private String EnforceSmsc; // simulator for fake dlrs
	private boolean downAlert; // alert in case of disconnection
	private String downEmail;
	private String downNumber;
	private boolean resend; // signaling route
	private boolean expireLongBroken; // generate EXPIRED Dlr if all parts not received in case of multipart pdu
	private String remark;
	private boolean sourceAsDest;
	private boolean greekEncode;
	private boolean enforceTonNpi;
	private int ston;
	private int snpi;
	private int dton;
	private int dnpi;
	private boolean enforceDlr;
	private String category;
	private String destPrefix;
	private boolean replaceContent;
	private String replaceContentText;
	private boolean multipart;
	private String customDlrTime;
	private long deliveryWaitTime;
	private boolean createPartDlr;
	private boolean enforceDefaultEsm;
	private String priorSender;
	private int backupPort;
	private String backupIp1;
	private int backupPort1;
	private boolean skipDlt;
	private String skipHlrSender;
	private int delayedDlr;
	private String alertUrl;
	private String dndSource;
	private boolean rlzRespId;
	private int maxLatency;
	// --------- optional -------------
	private String[] names;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getBackupIp() {
		return backupIp;
	}

	public void setBackupIp(String backupIp) {
		this.backupIp = backupIp;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getBindMode() {
		return bindMode;
	}

	public void setBindMode(String bindMode) {
		this.bindMode = bindMode;
	}

	public int getSleep() {
		return sleep;
	}

	public void setSleep(int sleep) {
		this.sleep = sleep;
	}

	public boolean isBind() {
		return bind;
	}

	public void setBind(boolean bind) {
		this.bind = bind;
	}

	public boolean isReplaceSource() {
		return replaceSource;
	}

	public void setReplaceSource(boolean replaceSource) {
		this.replaceSource = replaceSource;
	}

	public String getDefaultSource() {
		return defaultSource;
	}

	public void setDefaultSource(String defaultSource) {
		this.defaultSource = defaultSource;
	}

	public String getAllowedSources() {
		return allowedSources;
	}

	public void setAllowedSources(String allowedSources) {
		this.allowedSources = allowedSources;
	}

	public boolean isDestRestrict() {
		return destRestrict;
	}

	public void setDestRestrict(boolean destRestrict) {
		this.destRestrict = destRestrict;
	}

	public int getMinDestTime() {
		return minDestTime;
	}

	public void setMinDestTime(int minDestTime) {
		this.minDestTime = minDestTime;
	}

	public boolean isLooping() {
		return looping;
	}

	public void setLooping(boolean looping) {
		this.looping = looping;
	}

	public int getLoopCount() {
		return loopCount;
	}

	public void setLoopCount(int loopCount) {
		this.loopCount = loopCount;
	}

	public int getLoopDuration() {
		return loopDuration;
	}

	public void setLoopDuration(int loopDuration) {
		this.loopDuration = loopDuration;
	}

	public boolean isHexResponse() {
		return hexResponse;
	}

	public void setHexResponse(boolean hexResponse) {
		this.hexResponse = hexResponse;
	}

	public String getEnforceSmsc() {
		return EnforceSmsc;
	}

	public void setEnforceSmsc(String enforceSmsc) {
		EnforceSmsc = enforceSmsc;
	}

	public boolean isDownAlert() {
		return downAlert;
	}

	public void setDownAlert(boolean downAlert) {
		this.downAlert = downAlert;
	}

	public String getDownEmail() {
		return downEmail;
	}

	public void setDownEmail(String downEmail) {
		this.downEmail = downEmail;
	}

	public String getDownNumber() {
		return downNumber;
	}

	public void setDownNumber(String downNumber) {
		this.downNumber = downNumber;
	}

	public boolean isResend() {
		return resend;
	}

	public void setResend(boolean resend) {
		this.resend = resend;
	}

	public boolean isExpireLongBroken() {
		return expireLongBroken;
	}

	public void setExpireLongBroken(boolean expireLongBroken) {
		this.expireLongBroken = expireLongBroken;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public boolean isSourceAsDest() {
		return sourceAsDest;
	}

	public void setSourceAsDest(boolean sourceAsDest) {
		this.sourceAsDest = sourceAsDest;
	}

	public boolean isGreekEncode() {
		return greekEncode;
	}

	public void setGreekEncode(boolean greekEncode) {
		this.greekEncode = greekEncode;
	}

	public boolean isEnforceTonNpi() {
		return enforceTonNpi;
	}

	public void setEnforceTonNpi(boolean enforceTonNpi) {
		this.enforceTonNpi = enforceTonNpi;
	}

	public int getSton() {
		return ston;
	}

	public void setSton(int ston) {
		this.ston = ston;
	}

	public int getSnpi() {
		return snpi;
	}

	public void setSnpi(int snpi) {
		this.snpi = snpi;
	}

	public int getDton() {
		return dton;
	}

	public void setDton(int dton) {
		this.dton = dton;
	}

	public int getDnpi() {
		return dnpi;
	}

	public void setDnpi(int dnpi) {
		this.dnpi = dnpi;
	}

	public boolean isEnforceDlr() {
		return enforceDlr;
	}

	public void setEnforceDlr(boolean enforceDlr) {
		this.enforceDlr = enforceDlr;
	}

	public String[] getNames() {
		return names;
	}

	public void setNames(String[] names) {
		this.names = names;
	}

	public String getDestPrefix() {
		return destPrefix;
	}

	public void setDestPrefix(String destPrefix) {
		this.destPrefix = destPrefix;
	}

	public String getCustomDlrTime() {
		return customDlrTime;
	}

	public void setCustomDlrTime(String customDlrTime) {
		this.customDlrTime = customDlrTime;
	}

	public boolean isReplaceContent() {
		return replaceContent;
	}

	public void setReplaceContent(boolean replaceContent) {
		this.replaceContent = replaceContent;
	}

	public String getReplaceContentText() {
		return replaceContentText;
	}

	public void setReplaceContentText(String replaceContentText) {
		this.replaceContentText = replaceContentText;
	}

	public long getDeliveryWaitTime() {
		return deliveryWaitTime;
	}

	public void setDeliveryWaitTime(long deliveryWaitTime) {
		this.deliveryWaitTime = deliveryWaitTime;
	}

	public String getCategory() {
		if (category == null) {
			category = "Wholesale";
		}
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public boolean isMultipart() {
		return multipart;
	}

	public void setMultipart(boolean multipart) {
		this.multipart = multipart;
	}

	public boolean isCreatePartDlr() {
		return createPartDlr;
	}

	public void setCreatePartDlr(boolean createPartDlr) {
		this.createPartDlr = createPartDlr;
	}

	public boolean isEnforceDefaultEsm() {
		return enforceDefaultEsm;
	}

	public void setEnforceDefaultEsm(boolean enforceDefaultEsm) {
		this.enforceDefaultEsm = enforceDefaultEsm;
	}

	public String getPriorSender() {
		return priorSender;
	}

	public void setPriorSender(String priorSender) {
		this.priorSender = priorSender;
	}

	public int getBackupPort() {
		return backupPort;
	}

	public void setBackupPort(int backupPort) {
		this.backupPort = backupPort;
	}

	public String getBackupIp1() {
		return backupIp1;
	}

	public void setBackupIp1(String backupIp1) {
		this.backupIp1 = backupIp1;
	}

	public int getBackupPort1() {
		return backupPort1;
	}

	public void setBackupPort1(int backupPort1) {
		this.backupPort1 = backupPort1;
	}

	public boolean isSkipDlt() {
		return skipDlt;
	}

	public void setSkipDlt(boolean skipDlt) {
		this.skipDlt = skipDlt;
	}

	public String getSkipHlrSender() {
		return skipHlrSender;
	}

	public void setSkipHlrSender(String skipHlrSender) {
		this.skipHlrSender = skipHlrSender;
	}

	public int getDelayedDlr() {
		return delayedDlr;
	}

	public void setDelayedDlr(int delayedDlr) {
		this.delayedDlr = delayedDlr;
	}

	public String getAlertUrl() {
		return alertUrl;
	}

	public void setAlertUrl(String alertUrl) {
		this.alertUrl = alertUrl;
	}

	public String getDndSource() {
		return dndSource;
	}

	public void setDndSource(String dndSource) {
		this.dndSource = dndSource;
	}

	public boolean isRlzRespId() {
		return rlzRespId;
	}

	public void setRlzRespId(boolean rlzRespId) {
		this.rlzRespId = rlzRespId;
	}

	public int getMaxLatency() {
		return maxLatency;
	}

	public void setMaxLatency(int maxLatency) {
		this.maxLatency = maxLatency;
	}
}
