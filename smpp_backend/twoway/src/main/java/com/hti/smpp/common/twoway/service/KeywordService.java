package com.hti.smpp.common.twoway.service;

import java.util.Collection;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.hti.smpp.common.twoway.dto.KeywordEntry;
import com.hti.smpp.common.twoway.request.KeywordEntryForm;
import com.hti.smpp.common.twoway.request.TwowayReportForm;
import com.hti.smpp.common.user.dto.UserEntry;
/**
 * The `KeywordService` interface defines methods for handling keyword-related operations.
 */
public interface KeywordService {
	
	public ResponseEntity<String> addKeyword(KeywordEntryForm form, String username);
    public ResponseEntity<List<KeywordEntry>> listKeyword(String username);
    public ResponseEntity<String> updateKeyword(KeywordEntryForm form, String username); 
    public ResponseEntity<String> deleteKeyword(int id, String username);
    public ResponseEntity<Collection<UserEntry>> setupKeyword(String username);
    public ResponseEntity<KeywordEntry> viewKeyword(int id, String username);
    public ResponseEntity<StreamingResponseBody> generateXls(TwowayReportForm form, String locale, String username);
    public ResponseEntity<StreamingResponseBody> generatePdf(TwowayReportForm form, String locale, String username);
    public ResponseEntity<StreamingResponseBody> generateDoc(TwowayReportForm form, String locale, String username);
    public ResponseEntity<?> view(TwowayReportForm form, String locale, String username);
    public ResponseEntity<Collection<UserEntry>> setupTwowayReport(String username);
}
