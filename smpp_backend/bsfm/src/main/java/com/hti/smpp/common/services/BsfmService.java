package com.hti.smpp.common.services;

import com.hti.smpp.common.request.BsfmFilterFrom;
import com.hti.smpp.common.response.BSFMResponse;
import com.hti.smpp.common.response.DeleteProfileResponse;

public interface BsfmService {

	public String addBsfmProfile(BsfmFilterFrom bsfmFilterFrom, String username) throws Exception;

	public BSFMResponse checked(String username);

	public DeleteProfileResponse deleteProfile(String username, int id);

	public void showBsfmProfile(String username);

}
