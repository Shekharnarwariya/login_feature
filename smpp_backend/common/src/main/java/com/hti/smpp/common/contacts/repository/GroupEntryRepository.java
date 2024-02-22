package com.hti.smpp.common.contacts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.contacts.dto.GroupEntry;

@Repository
public interface GroupEntryRepository extends JpaRepository<GroupEntry, Integer> {

	
}
