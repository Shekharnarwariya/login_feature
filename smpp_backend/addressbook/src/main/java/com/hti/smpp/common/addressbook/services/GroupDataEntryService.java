package com.hti.smpp.common.addressbook.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.addressbook.request.GroupDataEntryRequest;
import com.hti.smpp.common.addressbook.request.SearchCriteria;
import com.hti.smpp.common.addressbook.response.ContactForBulk;
import com.hti.smpp.common.contacts.dto.GroupDataEntry;
/**
 * Interface for GroupDataEntryService defining group data-related operations.
 */
public interface GroupDataEntryService {

	public ResponseEntity<?> saveGroupData(String request, MultipartFile file, String username);

	public ResponseEntity<?> groupDataForBulk(List<Long> numbers, int groupId, String username);

	public ResponseEntity<List<GroupDataEntry>> viewSearchGroupData(SearchCriteria criteria, String username);

	public ResponseEntity<ContactForBulk> proceedSearchGroupData(SearchCriteria criteria, String username);

	public ResponseEntity<?> modifyGroupDataUpdate(GroupDataEntryRequest request, String username);

	public ResponseEntity<?> modifyGroupDataDelete(List<Integer> ids, String username);

	public ResponseEntity<?> modifyGroupDataExport(GroupDataEntryRequest request, String username);

	public ResponseEntity<?> editGroupDataSearch(int groupId, String username);
	
	public Page<GroupDataEntry> getGroupDataEntryByGroupId(int groupId, PageRequest pageRequest, String username);
}
