package com.hti.smpp.common.request;

import java.util.Map;

public class SendAreaSmsRequest {

	  private String areaArr[];
	  private String areaWiseNumber[];
	  private Map<?,?> numberMap;
	public String[] getAreaArr() {
		return areaArr;
	}
	public void setAreaArr(String[] areaArr) {
		this.areaArr = areaArr;
	}
	public String[] getAreaWiseNumber() {
		return areaWiseNumber;
	}
	public void setAreaWiseNumber(String[] areaWiseNumber) {
		this.areaWiseNumber = areaWiseNumber;
	}
	public Map<?, ?> getNumberMap() {
		return numberMap;
	}
	public void setNumberMap(Map<?, ?> numberMap) {
		this.numberMap = numberMap;
	}
	  
	  
}
