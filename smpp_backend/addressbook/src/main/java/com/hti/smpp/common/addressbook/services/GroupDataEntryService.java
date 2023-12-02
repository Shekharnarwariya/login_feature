package com.hti.smpp.common.addressbook.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.addressbook.request.GroupDataEntryRequest;
import com.hti.smpp.common.addressbook.response.ContactForBulk;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;

public interface GroupDataEntryService {
	
	public ResponseEntity<?> saveGroupData(String request, MultipartFile file, String username);
	public ContactForBulk groupDataForBulk(GroupDataEntryRequest groupDataEntry, String username);
	public List<GroupDataEntry> viewSearchGroupData(GroupDataEntryRequest request, String username);
	public ContactForBulk proceedSearchGroupData(GroupDataEntryRequest request, String username);
	public ResponseEntity<?> modifyGroupDataUpdate(GroupDataEntryRequest request, String username);
	public ResponseEntity<?> modifyGroupDataDelete(List<Integer> ids, String username);
	public ResponseEntity<?> modifyGroupDataExport(GroupDataEntryRequest request, String username);
	public ResponseEntity<?> editGroupDataSearch(int groupId, String username);
}
