package com.hti.smpp.common.request;

import java.util.List;

public class ChooseRequest {

	 private List<?> numberList;
	 private String actionReq;
	    private String actionDo;
	    private String sendNowMsgCount;
	    private String daysPart;
		public List<?> getNumberList() {
			return numberList;
		}
		public void setNumberList(List<?> numberList) {
			this.numberList = numberList;
		}
		public String getActionReq() {
			return actionReq;
		}
		public void setActionReq(String actionReq) {
			this.actionReq = actionReq;
		}
		public String getActionDo() {
			return actionDo;
		}
		public void setActionDo(String actionDo) {
			this.actionDo = actionDo;
		}
		public String getSendNowMsgCount() {
			return sendNowMsgCount;
		}
		public void setSendNowMsgCount(String sendNowMsgCount) {
			this.sendNowMsgCount = sendNowMsgCount;
		}
		public String getDaysPart() {
			return daysPart;
		}
		public void setDaysPart(String daysPart) {
			this.daysPart = daysPart;
		}
	    
	    
}
