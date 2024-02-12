
package com.hti.smpp.common.messages.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.stereotype.Component;

import com.hti.smpp.common.util.MultiUtility;

@Component
public class BulkSmsDTO implements Serializable {

	private String clientId;
	private String senderId;
	private String ston;
	private String snpi;
	// private transient MultipartFile destinationNumberFile;
	// private transient FormFile destinationNumberFile;
	private String dton;
	private String dnpi;
	private String message;
	private int count;
	private String esmClass;
	private String dcsValue;
	private List<String> destinationList;
	private String systemId;
	private String password;
	private String header;
	private String destinationNumber;
	private String from;
	private String sday;
	private String smonth;
	private String syear;
	private String hours;
	private String minutes;
	private String time;
	private String date;
	private String greet;
	private String asciiList;
	private String endday;
	private String endmonth;
	private String endyear;
	private String endhours;
	private String endminutes;
	private String endtime;
	private String enddate;
	private String timeship;
	private String timestart;
	private String timeend;
	private String smscName;
	private String username; // For Resend Module // Done by Sameer
	private String sourceno;
	private String smsc;
	private String resmsc;
	private String seconds;
	private String endseconds;
	private String uploadedNumbers;
	private String smscId;
	private String[] smscList;
	private String name;
	private String totalNumbers;
	private String fileName = null;
	private String complete;
	private double totalWalletCost;
	private String userMode;
	private String gmt;
	private String smscount;
	private String reqType;
	private boolean customContent;
	private String totalSmsParDay;
	private String distinct = "no";
	private String[] numberlist;
	private String reCheck;
	private int id[];
	private String[] user;
	private Map<String, List<String>> mapTable;
	private double delay;
	private String repeat;
	private double totalCost;
	private int fileid;
	private boolean isSchedule;
	private boolean isAlert;
	private long msgCount;
	private String temp;
	// ------------ Modified on 27-Feb-2016 ---------------
	private String origMessage; // For summary
	private String messageType; // Encoding
	private int smsParts; // Sms Parts
	private int charCount; // Char Count
	private int charLimit; // Char limit per sms
	private String exclude; // excluded numbers from uploaded file
	private String status; // status running/paused
	private long expiryHour;
	private boolean allowDuplicate;
	private String campaignName;
	private String campaignType = "Immediate";
	// ------ optional parameters for submit_sm ------
	private String peId;
	private String templateId;
	private String telemarketerId;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getSton() {
		return ston;
	}

	public void setSton(String ston) {
		this.ston = ston;
	}

	public String getSnpi() {
		return snpi;
	}

	public void setSnpi(String snpi) {
		this.snpi = snpi;
	}

	public String getDton() {
		return dton;
	}

	public void setDton(String dton) {
		this.dton = dton;
	}

	public String getDnpi() {
		return dnpi;
	}

	public void setDnpi(String dnpi) {
		this.dnpi = dnpi;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getEsmClass() {
		return esmClass;
	}

	public void setEsmClass(String esmClass) {
		this.esmClass = esmClass;
	}

	public String getDcsValue() {
		return dcsValue;
	}

	public void setDcsValue(String dcsValue) {
		this.dcsValue = dcsValue;
	}

	public List<String> getDestinationList() {
		return destinationList;
	}

	public void setDestinationList(List<String> destinationList) {
		this.destinationList = destinationList;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSday() {
		return sday;
	}

	public void setSday(String sday) {
		this.sday = sday;
	}

	public String getSmonth() {
		return smonth;
	}

	public void setSmonth(String smonth) {
		this.smonth = smonth;
	}

	public String getSyear() {
		return syear;
	}

	public void setSyear(String syear) {
		this.syear = syear;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	public String getMinutes() {
		return minutes;
	}

	public void setMinutes(String minutes) {
		this.minutes = minutes;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getGreet() {
		return greet;
	}

	public void setGreet(String greet) {
		this.greet = greet;
	}

	public String getAsciiList() {
		return asciiList;
	}

	public void setAsciiList(String asciiList) {
		this.asciiList = asciiList;
	}

	public String getEndday() {
		return endday;
	}

	public void setEndday(String endday) {
		this.endday = endday;
	}

	public String getEndmonth() {
		return endmonth;
	}

	public void setEndmonth(String endmonth) {
		this.endmonth = endmonth;
	}

	public String getEndyear() {
		return endyear;
	}

	public void setEndyear(String endyear) {
		this.endyear = endyear;
	}

	public String getEndhours() {
		return endhours;
	}

	public void setEndhours(String endhours) {
		this.endhours = endhours;
	}

	public String getEndminutes() {
		return endminutes;
	}

	public void setEndminutes(String endminutes) {
		this.endminutes = endminutes;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public String getEnddate() {
		return enddate;
	}

	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}

	public String getTimeship() {
		return timeship;
	}

	public void setTimeship(String timeship) {
		this.timeship = timeship;
	}

	public String getTimestart() {
		return timestart;
	}

	public void setTimestart(String timestart) {
		this.timestart = timestart;
	}

	public String getTimeend() {
		return timeend;
	}

	public void setTimeend(String timeend) {
		this.timeend = timeend;
	}

	public String getSmscName() {
		return smscName;
	}

	public void setSmscName(String smscName) {
		this.smscName = smscName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSourceno() {
		return sourceno;
	}

	public void setSourceno(String sourceno) {
		this.sourceno = sourceno;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public String getResmsc() {
		return resmsc;
	}

	public void setResmsc(String resmsc) {
		this.resmsc = resmsc;
	}

	public String getSeconds() {
		return seconds;
	}

	public void setSeconds(String seconds) {
		this.seconds = seconds;
	}

	public String getEndseconds() {
		return endseconds;
	}

	public void setEndseconds(String endseconds) {
		this.endseconds = endseconds;
	}

	public String getUploadedNumbers() {
		return uploadedNumbers;
	}

	public void setUploadedNumbers(String uploadedNumbers) {
		this.uploadedNumbers = uploadedNumbers;
	}

	public String getSmscId() {
		return smscId;
	}

	public void setSmscId(String smscId) {
		this.smscId = smscId;
	}

	public String[] getSmscList() {
		return smscList;
	}

	public void setSmscList(String[] smscList) {
		this.smscList = smscList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTotalNumbers() {
		return totalNumbers;
	}

	public void setTotalNumbers(String totalNumbers) {
		this.totalNumbers = totalNumbers;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getComplete() {
		return complete;
	}

	public void setComplete(String complete) {
		this.complete = complete;
	}

	public double getTotalWalletCost() {
		return totalWalletCost;
	}

	public void setTotalWalletCost(double totalWalletCost) {
		this.totalWalletCost = totalWalletCost;
	}

	public String getUserMode() {
		return userMode;
	}

	public void setUserMode(String userMode) {
		this.userMode = userMode;
	}

	public String getGmt() {
		return gmt;
	}

	public void setGmt(String gmt) {
		this.gmt = gmt;
	}

	public String getSmscount() {
		return smscount;
	}

	public void setSmscount(String smscount) {
		this.smscount = smscount;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public boolean isCustomContent() {
		return customContent;
	}

	public void setCustomContent(boolean customContent) {
		this.customContent = customContent;
	}

	public String getTotalSmsParDay() {
		return totalSmsParDay;
	}

	public void setTotalSmsParDay(String totalSmsParDay) {
		this.totalSmsParDay = totalSmsParDay;
	}

	public String getDistinct() {
		return distinct;
	}

	public void setDistinct(String distinct) {
		this.distinct = distinct;
	}

	public String[] getNumberlist() {
		return numberlist;
	}

	public void setNumberlist(String[] numberlist) {
		this.numberlist = numberlist;
	}

	public String getReCheck() {
		return reCheck;
	}

	public void setReCheck(String reCheck) {
		this.reCheck = reCheck;
	}

	public int[] getId() {
		return id;
	}

	public void setId(int[] id) {
		this.id = id;
	}

	public String[] getUser() {
		return user;
	}

	public void setUser(String[] user) {
		this.user = user;
	}

	public Map<String, List<String>> getMapTable() {
		return mapTable;
	}

	public void setMapTable(Map<String, List<String>> mapTable) {
		this.mapTable = mapTable;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}

	public int getFileid() {
		return fileid;
	}

	public void setFileid(int fileid) {
		this.fileid = fileid;
	}

	public boolean isSchedule() {
		return isSchedule;
	}

	public void setSchedule(boolean isSchedule) {
		this.isSchedule = isSchedule;
	}

	public boolean isAlert() {
		return isAlert;
	}

	public void setAlert(boolean isAlert) {
		this.isAlert = isAlert;
	}

	public long getMsgCount() {
		return msgCount;
	}

	public void setMsgCount(long msgCount) {
		this.msgCount = msgCount;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getOrigMessage() {
		return origMessage;
	}

	public void setOrigMessage(String origMessage) {
		this.origMessage = origMessage;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public int getSmsParts() {
		return smsParts;
	}

	public void setSmsParts(int smsParts) {
		this.smsParts = smsParts;
	}

	public int getCharCount() {
		return charCount;
	}

	public void setCharCount(int charCount) {
		this.charCount = charCount;
	}

	public int getCharLimit() {
		return charLimit;
	}

	public void setCharLimit(int charLimit) {
		this.charLimit = charLimit;
	}

	public String getExclude() {
		return exclude;
	}

	public void setExclude(String exclude) {
		this.exclude = exclude;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getExpiryHour() {
		return expiryHour;
	}

	public void setExpiryHour(long expiryHour) {
		this.expiryHour = expiryHour;
	}

	public boolean isAllowDuplicate() {
		return allowDuplicate;
	}

	public void setAllowDuplicate(boolean allowDuplicate) {
		this.allowDuplicate = allowDuplicate;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public String getPeId() {
		return peId;
	}

	public void setPeId(String peId) {
		this.peId = peId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTelemarketerId() {
		return telemarketerId;
	}

	public void setTelemarketerId(String telemarketerId) {
		this.telemarketerId = telemarketerId;
	}

	public BulkSmsDTO(String clientId, String senderId, String ston, String snpi, String dton, String dnpi,
			String message, int count, String esmClass, String dcsValue, List<String> destinationList, String systemId,
			String password, String header, String destinationNumber, String from, String sday, String smonth,
			String syear, String hours, String minutes, String time, String date, String greet, String asciiList,
			String endday, String endmonth, String endyear, String endhours, String endminutes, String endtime,
			String enddate, String timeship, String timestart, String timeend, String smscName, String username,
			String sourceno, String smsc, String resmsc, String seconds, String endseconds, String uploadedNumbers,
			String smscId, String[] smscList, String name, String totalNumbers, String fileName, String complete,
			double totalWalletCost, String userMode, String gmt, String smscount, String reqType, boolean customContent,
			String totalSmsParDay, String distinct, String[] numberlist, String reCheck, int[] id, String[] user,
			Map<String, List<String>> mapTable, double delay, String repeat, double totalCost, int fileid,
			boolean isSchedule, boolean isAlert, long msgCount, String temp, String origMessage, String messageType,
			int smsParts, int charCount, int charLimit, String exclude, String status, long expiryHour,
			boolean allowDuplicate, String campaignName, String campaignType, String peId, String templateId,
			String telemarketerId) {
		super();
		this.clientId = clientId;
		this.senderId = senderId;
		this.ston = ston;
		this.snpi = snpi;
		this.dton = dton;
		this.dnpi = dnpi;
		this.message = message;
		this.count = count;
		this.esmClass = esmClass;
		this.dcsValue = dcsValue;
		this.destinationList = destinationList;
		this.systemId = systemId;
		this.password = password;
		this.header = header;
		this.destinationNumber = destinationNumber;
		this.from = from;
		this.sday = sday;
		this.smonth = smonth;
		this.syear = syear;
		this.hours = hours;
		this.minutes = minutes;
		this.time = time;
		this.date = date;
		this.greet = greet;
		this.asciiList = asciiList;
		this.endday = endday;
		this.endmonth = endmonth;
		this.endyear = endyear;
		this.endhours = endhours;
		this.endminutes = endminutes;
		this.endtime = endtime;
		this.enddate = enddate;
		this.timeship = timeship;
		this.timestart = timestart;
		this.timeend = timeend;
		this.smscName = smscName;
		this.username = username;
		this.sourceno = sourceno;
		this.smsc = smsc;
		this.resmsc = resmsc;
		this.seconds = seconds;
		this.endseconds = endseconds;
		this.uploadedNumbers = uploadedNumbers;
		this.smscId = smscId;
		this.smscList = smscList;
		this.name = name;
		this.totalNumbers = totalNumbers;
		this.fileName = fileName;
		this.complete = complete;
		this.totalWalletCost = totalWalletCost;
		this.userMode = userMode;
		this.gmt = gmt;
		this.smscount = smscount;
		this.reqType = reqType;
		this.customContent = customContent;
		this.totalSmsParDay = totalSmsParDay;
		this.distinct = distinct;
		this.numberlist = numberlist;
		this.reCheck = reCheck;
		this.id = id;
		this.user = user;
		this.mapTable = mapTable;
		this.delay = delay;
		this.repeat = repeat;
		this.totalCost = totalCost;
		this.fileid = fileid;
		this.isSchedule = isSchedule;
		this.isAlert = isAlert;
		this.msgCount = msgCount;
		this.temp = temp;
		this.origMessage = origMessage;
		this.messageType = messageType;
		this.smsParts = smsParts;
		this.charCount = charCount;
		this.charLimit = charLimit;
		this.exclude = exclude;
		this.status = status;
		this.expiryHour = expiryHour;
		this.allowDuplicate = allowDuplicate;
		this.campaignName = campaignName;
		this.campaignType = campaignType;
		this.peId = peId;
		this.templateId = templateId;
		this.telemarketerId = telemarketerId;
	}

	public BulkSmsDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "BulkSmsDTO [clientId=" + clientId + ", senderId=" + senderId + ", ston=" + ston + ", snpi=" + snpi
				+ ", dton=" + dton + ", dnpi=" + dnpi + ", message=" + message + ", count=" + count + ", esmClass="
				+ esmClass + ", dcsValue=" + dcsValue + ", destinationList=" + destinationList + ", systemId="
				+ systemId + ", password=" + password + ", header=" + header + ", destinationNumber="
				+ destinationNumber + ", from=" + from + ", sday=" + sday + ", smonth=" + smonth + ", syear=" + syear
				+ ", hours=" + hours + ", minutes=" + minutes + ", time=" + time + ", date=" + date + ", greet=" + greet
				+ ", asciiList=" + asciiList + ", endday=" + endday + ", endmonth=" + endmonth + ", endyear=" + endyear
				+ ", endhours=" + endhours + ", endminutes=" + endminutes + ", endtime=" + endtime + ", enddate="
				+ enddate + ", timeship=" + timeship + ", timestart=" + timestart + ", timeend=" + timeend
				+ ", smscName=" + smscName + ", username=" + username + ", sourceno=" + sourceno + ", smsc=" + smsc
				+ ", resmsc=" + resmsc + ", seconds=" + seconds + ", endseconds=" + endseconds + ", uploadedNumbers="
				+ uploadedNumbers + ", smscId=" + smscId + ", smscList=" + Arrays.toString(smscList) + ", name=" + name
				+ ", totalNumbers=" + totalNumbers + ", fileName=" + fileName + ", complete=" + complete
				+ ", totalWalletCost=" + totalWalletCost + ", userMode=" + userMode + ", gmt=" + gmt + ", smscount="
				+ smscount + ", reqType=" + reqType + ", customContent=" + customContent + ", totalSmsParDay="
				+ totalSmsParDay + ", distinct=" + distinct + ", numberlist=" + Arrays.toString(numberlist)
				+ ", reCheck=" + reCheck + ", id=" + Arrays.toString(id) + ", user=" + Arrays.toString(user)
				+ ", mapTable=" + mapTable + ", delay=" + delay + ", repeat=" + repeat + ", totalCost=" + totalCost
				+ ", fileid=" + fileid + ", isSchedule=" + isSchedule + ", isAlert=" + isAlert + ", msgCount="
				+ msgCount + ", temp=" + temp + ", origMessage=" + origMessage + ", messageType=" + messageType
				+ ", smsParts=" + smsParts + ", charCount=" + charCount + ", charLimit=" + charLimit + ", exclude="
				+ exclude + ", status=" + status + ", expiryHour=" + expiryHour + ", allowDuplicate=" + allowDuplicate
				+ ", campaignName=" + campaignName + ", campaignType=" + campaignType + ", peId=" + peId
				+ ", templateId=" + templateId + ", telemarketerId=" + telemarketerId + "]";
	}

	public List<String> getDestinationList2(BulkListInfo listInfo) {
		// Generate ArrayList
		List<String> noList = new ArrayList<String>();
		System.out.println("Count :" + getTotalNumbers());
		// System.out.println("numberlist :"+numberlist.length);
		String uploadedNumbers = getUploadedNumbers();
		Set<String> excludeSet = new HashSet<String>();
		if (getExclude() != null && getExclude().length() > 0) {
			String seperator = ",";
			if (getExclude().contains(",")) {
				seperator = ",";
			} else {
				seperator = "\n";
			}
			StringTokenizer tokens = new StringTokenizer(getExclude(), seperator);
			while (tokens.hasMoreTokens()) {
				String next = tokens.nextToken();
				if (next != null && next.length() > 0) {
					next = next.replaceAll("\\s+", ""); // Replace all the spaces in the String with empty character.
					try {
						long num = Long.parseLong(next);
						excludeSet.add(String.valueOf(num));
					} catch (NumberFormatException ne) {
						System.out.println("Invalid Exclude Number Found: " + next);
					}
				}
			}
		}
		try {
			String savedExcludeNumbers = MultiUtility.readExcludeNumbers(username);
			if (savedExcludeNumbers != null) {
				for (String excluded : savedExcludeNumbers.split("\n")) {
					try {
						long num = Long.parseLong(excluded);
						excludeSet.add(String.valueOf(num));
					} catch (NumberFormatException ne) {
						System.out.println(username + " Invalid Exclude Number Found: " + excluded);
					}
				}
			}
		} catch (Exception ex) {
			System.out.println(username + " " + ex);
		}
		if (!excludeSet.isEmpty()) {
			try {
				MultiUtility.writeExcludeNumbers(username, String.join("\n", excludeSet));
			} catch (Exception ex) {
				System.out.println(username + " " + ex);
			}
		}
		int validCount = 0;
		int invalidCount = 0;
		int total = Integer.valueOf((getTotalNumbers()).trim());
		StringTokenizer stoken = new StringTokenizer(uploadedNumbers, "\n");
		int tok = stoken.countTokens();
		String destinationNumber = null;
		listInfo.setTotal(total);
		for (int i = 0; i < total; i++) {
			destinationNumber = stoken.nextToken();
			destinationNumber = destinationNumber.trim();
			if (destinationNumber == null || destinationNumber.equalsIgnoreCase("")
					|| destinationNumber.length() == 0) {
				invalidCount++;
			} else {
				if (destinationNumber.startsWith("+")) {
					destinationNumber = destinationNumber.substring(1, destinationNumber.length());
				}
				try {
					long value = Long.parseLong(destinationNumber);
					if (!excludeSet.contains(String.valueOf(value))) {
						noList.add(String.valueOf(value));
						validCount++;
					}
				} catch (NumberFormatException nfe) {
					System.out.println("Invalid Destination Number => " + destinationNumber);
					// Add to invalid destination Count
					invalidCount++;
				}
			}
		}
		Set<String> hashSet = new HashSet<String>(noList);
		destinationList = new ArrayList<String>(hashSet);
		Collections.sort(destinationList);
		listInfo.setValidCount(validCount);
		listInfo.setInvalidCount(invalidCount);
		// System.out.println("successfully return from get destination no list()");
		int dup = total - destinationList.size() - invalidCount;
		// System.out.println("duplicate===="+dup);
		listInfo.setDuplicate(dup);
		// System.out.println("invalidCount : " + invalidCount);
		// System.out.println("Destination No list size is :" + destinationList.size());
		return destinationList;
	}

}
