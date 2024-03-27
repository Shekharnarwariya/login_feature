package com.hti.smpp.common.contacts.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.contacts.dto.GroupEntryDTO;

@Repository
public interface GroupEntryDTORepository extends JpaRepository<GroupEntryDTO, Integer>{
	
	public Page<GroupEntryDTO> findByMasterIdAndGroupData(String masterId, boolean groupData,Pageable pageable);
	public Page<GroupEntryDTO> findByMasterId(String masterId,Pageable pageable);
	public List<GroupEntryDTO> findByMasterId(String masterId);
}
