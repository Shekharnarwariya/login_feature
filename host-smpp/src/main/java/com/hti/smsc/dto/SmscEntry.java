package com.hti.smsc.dto;

import java.io.Serializable;

public class SmscEntry implements Serializable {
	private int id;
	private String name;
	private String systemId;
	private String masterId;
	private String password;
	private String ip;
	private int port;
	private String backupIp;
	private int backupPort;
	private String backupIp1;
	private int backupPort1;
	private String systemType;
	private String bindMode;
	private int sleep;
	private boolean replaceSource; // replace senderId if not found in allowed list
	private String defaultSource; // default senderId to be replaced
	private String allowedSources; // comma separated allowed senderId list
	private boolean destRestrict; // Restrict same destination under specific duration
	private int minDestTime; // duration between 2 duplicate destination
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
	private boolean skipDlt;
	private String skipHlrSender;
	private int delayedDlr;
	private String alertUrl;
	private String dndSource;
	private boolean rlzRespId;
	private int maxLatency; // in case of minus customdlrtime

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
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

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
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

	/*public boolean isLooping() {
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
	}*/

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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDestPrefix() {
		return destPrefix;
	}

	public void setDestPrefix(String destPrefix) {
		this.destPrefix = destPrefix;
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

	public boolean isMultipart() {
		return multipart;
	}

	public void setMultipart(boolean multipart) {
		this.multipart = multipart;
	}

	public String getCustomDlrTime() {
		return customDlrTime;
	}

	public void setCustomDlrTime(String customDlrTime) {
		this.customDlrTime = customDlrTime;
	}

	public long getDeliveryWaitTime() {
		return deliveryWaitTime;
	}

	public void setDeliveryWaitTime(long deliveryWaitTime) {
		this.deliveryWaitTime = deliveryWaitTime;
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

	public String toString() {
		return "smsc: id=" + id + ",name=" + name + ",systemId=" + systemId + ",systemType=" + systemType + ",ip=" + ip
				+ ",backupIp=" + backupIp + ",port=" + port + ",bindmode=" + bindMode + ",sleep=" + sleep;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SmscEntry entry = (SmscEntry) o;
		return (id == entry.id) && (name == null ? entry.name == null : name.equals(entry.name))
				&& (systemId == null ? entry.systemId == null : systemId.equals(entry.systemId))
				&& (masterId == null ? entry.masterId == null : masterId.equals(entry.masterId))
				&& (password == null ? entry.password == null : password.equals(entry.password))
				&& (ip == null ? entry.ip == null : ip.equals(entry.ip))
				&& (backupIp == null ? entry.backupIp == null : backupIp.equals(entry.backupIp)) && (port == entry.port)
				&& (systemType == null ? entry.systemType == null : systemType.equals(entry.systemType))
				&& (bindMode == null ? entry.bindMode == null : bindMode.equals(entry.bindMode))
				&& (sleep == entry.sleep) && (replaceSource == entry.replaceSource)
				&& (defaultSource == null ? entry.defaultSource == null : defaultSource.equals(entry.defaultSource))
				&& (allowedSources == null ? entry.allowedSources == null : allowedSources.equals(entry.allowedSources))
				&& (destRestrict == entry.destRestrict) && (minDestTime == entry.minDestTime)
				//&& (looping == entry.looping) && (loopCount == entry.loopCount) && (loopDuration == entry.loopDuration)
				&& (hexResponse == entry.hexResponse)
				&& (EnforceSmsc == null ? entry.EnforceSmsc == null : EnforceSmsc.equals(entry.EnforceSmsc))
				&& (downAlert == entry.downAlert)
				&& (downEmail == null ? entry.downEmail == null : downEmail.equals(entry.downEmail))
				&& (downNumber == null ? entry.downNumber == null : downNumber.equals(entry.downNumber))
				&& (resend == entry.resend) && (expireLongBroken == entry.expireLongBroken)
				&& (remark == null ? entry.remark == null : remark.equals(entry.remark))
				&& (sourceAsDest == entry.sourceAsDest) && (greekEncode == entry.greekEncode)
				&& (enforceTonNpi == entry.enforceTonNpi) && (ston == entry.ston) && (snpi == entry.snpi)
				&& (dton == entry.dton) && (dnpi == entry.dnpi) && (enforceDlr == entry.enforceDlr)
				&& (category == null ? entry.category == null : category.equals(entry.category))
				&& (destPrefix == null ? entry.destPrefix == null : destPrefix.equals(entry.destPrefix))
				&& (replaceContent == entry.replaceContent) && (replaceContentText == null
						? entry.replaceContentText == null : replaceContentText.equals(entry.replaceContentText));
	}
}
