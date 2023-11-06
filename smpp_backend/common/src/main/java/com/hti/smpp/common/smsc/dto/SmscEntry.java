package com.hti.smpp.common.smsc.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "smscmaster")
public class SmscEntry implements Serializable {
	@Id
	@Column(name = "id", length = 5)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "name", unique = true, nullable = false, updatable = false)
	private String name;
	@Column(name = "system_id")
	private String systemId;
	@Column(name = "master_id")
	private String masterId;
	@Column(name = "password", nullable = false, updatable = false)
	private String password;
	@Column(name = "ip")
	private String ip;
	@Column(name = "port", length = 5)
	private int port;
	@Column(name = "backup_ip")
	private String backupIp;
	@Column(name = "backup_port", length = 5)
	private int backupPort;
	@Column(name = "backup_ip_1")
	private String backupIp1;
	@Column(name = "backup_port_1", length = 5)
	private int backupPort1;
	@Column(name = "system_type")
	private String systemType;
	@Column(name = "bindmode")
	private String bindMode;
	@Column(name = "sleep_time", length = 11)
	private int sleep;
	@Column(name = "replaceSource")
	private boolean replaceSource; // replace senderId if not found in allowed list
	@Column(name = "defaultSource")
	private String defaultSource; // default senderId to be replaced
	@Column(name = "allowedSources")
	private String allowedSources; // comma separated allowed senderId list
	@Column(name = "dstapply", length = 1)
	private boolean destRestrict; // Restrict same destination under specific duration
	@Column(name = "dest_time_gap", length = 10)
	private int minDestTime; // duration between 2 duplicate destination
	@Column(name = "loopApply", length = 1)
	private boolean looping; // duplicate packet(senderId + destination + content) restrictions
	@Column(name = "loop_count", length = 10)
	private int loopCount; // number of duplicate packets
	@Column(name = "loop_time_gap", length = 10)
	private int loopDuration; // duration for duplicate count
	@Column(name = "HexConvert", length = 1)
	private boolean hexResponse; // hexadecimal Submit response on submit
	@Column(name = "EnforceSMSC")
	private String EnforceSmsc; // simulator for fake dlrs
	@Column(name = "isAlert", length = 1)
	private boolean downAlert; // alert in case of disconnection
	@Column(name = "alert_email")
	private String downEmail;
	@Column(name = "alert_number")
	private String downNumber;
	@Column(name = "alert_url")
	private String alertUrl;
	@Column(name = "resend", length = 1)
	private boolean resend; // signaling route
	@Column(name = "expireLongBroken", length = 1)
	private boolean expireLongBroken; // generate EXPIRED Dlr if all parts not received in case of multipart pdu
	@Column(name = "remark")
	private String remark;
	@Column(name = "SameDestSource", length = 1)
	private boolean sourceAsDest;
	@Column(name = "Greek_Encode", length = 1)
	private boolean greekEncode;
	@Column(name = "enforcetonnpi", length = 1)
	private boolean enforceTonNpi;
	@Column(name = "ston", length = 11)
	private int ston;
	@Column(name = "snpi", length = 11)
	private int snpi;
	@Column(name = "dton", length = 11)
	private int dton;
	@Column(name = "dnpi", length = 11)
	private int dnpi;
	@Column(name = "EnforceDLR", length = 1)
	private boolean enforceDlr;
	@Column(name = "category")
	private String category;
	@Column(name = "dest_prefix")
	private String destPrefix;
	@Column(name = "isReplaceContent", length = 1)
	private boolean replaceContent;
	@Column(name = "content_replace")
	private String replaceContentText;
	@Column(name = "multipart", length = 1)
	private boolean multipart;
	@Column(name = "customDlrTime")
	private String customDlrTime;
	@Column(name = "delivWaitTime", length = 20)
	private long deliveryWaitTime;
	@Column(name = "create_part_dlr", length = 1)
	private boolean createPartDlr;
	@Column(name = "enforce_esm", length = 1)
	private boolean enforceDefaultEsm;
	@Column(name = "prior_sender")
	private String priorSender;
	@Column(name = "skip_dlt", length = 1)
	private boolean skipDlt;
	@Column(name = "skip_hlr_sender", columnDefinition = "TEXT")
	private String skipHlrSender;
	@Column(name = "dlr_delay", length = 3)
	private int delayedDlr;
	@Column(name = "dnd_source")
	private String dndSource;
	@Column(name = "rlz_resp_id", length = 1)
	private boolean rlzRespId;
	@Column(name = "max_latency", length = 3)
	private int maxLatency;

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
				&& (looping == entry.looping) && (loopCount == entry.loopCount) && (loopDuration == entry.loopDuration)
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
				&& (replaceContent == entry.replaceContent)
				&& (replaceContentText == null ? entry.replaceContentText == null
						: replaceContentText.equals(entry.replaceContentText));
	}
}
