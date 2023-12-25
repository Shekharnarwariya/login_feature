package com.hti.smpp.common.services;

import java.util.List;

import com.hti.smpp.common.bsfm.dto.Bsfm;
import com.hti.smpp.common.request.BsfmFilterFrom;
import com.hti.smpp.common.response.BSFMResponse;
import com.hti.smpp.common.response.DeleteProfileResponse;
/**
 * Service interface for Bsfm operations.
 */
public interface BsfmService {

	public String addBsfmProfile(BsfmFilterFrom bsfmFilterFrom, String username) throws Exception;

	public BSFMResponse checked(String username);

	public DeleteProfileResponse deleteProfile(String username, int id);

	public List<Bsfm> showBsfmProfile(String username);

	public String updateBsfmProfil(BsfmFilterFrom bsfmFilterFrom, String username);

	public String delete(String username, BsfmFilterFrom bsfmForm);

	public String updateBsfmProfileFlag(String username, BsfmFilterFrom bsfmForm);

}
