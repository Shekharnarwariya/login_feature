package com.hti.smpp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.request.CustomRequest;
import com.hti.smpp.common.request.GroupMemberRequest;
import com.hti.smpp.common.request.GroupRequest;
import com.hti.smpp.common.request.SmscEntryRequest;
import com.hti.smpp.common.service.SmscDAO;

@RestController
@RequestMapping("/smsc")
public class SmscController {

	@Autowired
	private SmscDAO smscDAOImpl;

	@PostMapping("/insert/smsc")
	public String saveSmscEntry(@RequestBody SmscEntryRequest smscEntryRequest,
			@RequestHeader("username") String username) {
		return smscDAOImpl.save(smscEntryRequest, username);
	}

	@PostMapping("/insert/custom")
	public String saveCustom(@RequestBody CustomRequest customRequest) {
		return smscDAOImpl.saveCustom(customRequest);
	}

	@PostMapping("/insert/group")
	public String saveGroup(@RequestBody GroupRequest groupRequest) {
		return smscDAOImpl.saveGroup(groupRequest);
	}

	@PostMapping("/insert/groupMember")
	public String saveGroupMember(@RequestBody GroupMemberRequest groupMemberRequest) {
		return smscDAOImpl.saveGroupMember(groupMemberRequest);
	}

}
