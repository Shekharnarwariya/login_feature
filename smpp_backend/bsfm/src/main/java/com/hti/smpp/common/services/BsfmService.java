package com.hti.smpp.common.services;

import com.hti.smpp.common.dto.BsfmFilterFrom;


public interface BsfmService {
	
	public void addBsfmProfile(BsfmFilterFrom bsfmFilterFrom, String username) throws Exception;
//	public List<Bsfm> showBsfmProfile(String masterId);
//	public void updateBsfmProfile(BsfmDto bsfm, String username) throws Exception;
//	public void deleteBsfmActiveProfile(String profilename) throws Exception;
//	public void bsfmDeleteProfile() throws Exception;
//	public boolean updateBsfmProfileFlag(String flag)throws Exception;
}
