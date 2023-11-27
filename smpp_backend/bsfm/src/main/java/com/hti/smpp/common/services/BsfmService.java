package com.hti.smpp.common.services;

import java.util.List;

import com.hti.smpp.common.bsfm.dto.Bsfm;
import com.hti.smpp.common.dto.BsfmDto;


public interface BsfmService {
	
	public void addBsfmProfile(BsfmDto bsfm, String username) throws Exception;
	public List<Bsfm> showBsfmProfile(String masterId);
	public void updateBsfmProfile(BsfmDto bsfm, String username) throws Exception;
	public void deleteBsfmActiveProfile(String profilename) throws Exception;
	public void bsfmDeleteProfile() throws Exception;
	public boolean updateBsfmProfileFlag(String flag)throws Exception;
}
