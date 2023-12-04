package com.hti.smpp.common.contacts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.contacts.dto.GroupEntryDTO;

@Repository
public interface GroupEntryDTORepository extends JpaRepository<GroupEntryDTO, Integer>{
	public List<GroupEntryDTO> findByMasterIdAndGroupData(String masterId, boolean groupData);
	public List<GroupEntryDTO> findByMasterId(String masterId);
}
