package com.hti.smpp.common.httpclient;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class IDatabaseService {

	public int removeApiStatus(String max_msg_id, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void putApiStatus(ApiRequestDTO requestDTO) {
		// TODO Auto-generated method stub

	}

	public List<BaseApiDTO> listApiSchedule() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeApiSchedule(int scheduleId) {
		// TODO Auto-generated method stub
		
	}

	public int removeHttpDlrParamLog(String string) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void saveHttpDlrParamLog(List<HttpDlrParamEntry> list) {
		// TODO Auto-generated method stub
		
	}

	public int removeHttpRequestLog(String string) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void saveHttpRequestLog(List<HttpRequestEntry> list) {
		// TODO Auto-generated method stub
		
	}

	public List<ApiResultDTO> getApiStatus(String batch_id, String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, ApiResultDTO> getDeliveryStatus(String username, List<String> toEnquireList) {
		// TODO Auto-generated method stub
		return null;
	}

	public BaseApiDTO getApiSchedule(String username, String batch_id) {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateVngStatus(String responseId, String status, String statusCode, String remarks) {
		// TODO Auto-generated method stub
		
	}

	public List<ApiResultDTO> listApiSchedule(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public int createApiSchedule(BaseApiDTO jsonDTO) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addLookupSummaryReport(LookupSummaryObj summary) {
		// TODO Auto-generated method stub
		
	}

	public boolean deleteschedule(String scheduleid, String user) {
		// TODO Auto-generated method stub
		return false;
	}

	public Map getDlrReport(String username, String query, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getMessageContent(Map map, String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getCustomizedReport(String username, String query, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}

	public List getUnprocessedReport(String replaceAll, boolean b, boolean isContent) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCountryname(String ip_address) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getDeliveryStatus(String username, String msgid) {
		// TODO Auto-generated method stub
		return null;
	}

}
