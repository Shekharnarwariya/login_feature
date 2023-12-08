package com.hti.smpp.common.twoway.service;

import java.util.Collection;
import java.util.List;

import com.hti.smpp.common.twoway.dto.KeywordEntry;
import com.hti.smpp.common.twoway.request.KeywordEntryForm;
import com.hti.smpp.common.user.dto.UserEntry;

public interface KeywordService {
	
	public String addKeyword(KeywordEntryForm form, String username);
    public List<KeywordEntry> listKeyword(String username);
    public String updateKeyword(KeywordEntryForm form, String username); 
    public String deleteKeyword(int id, String username);
    public Collection<UserEntry> setupKeyword(String username);
    public KeywordEntry viewKeyword(int id, String username);
}
