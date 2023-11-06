package com.hti.smpp.common.contacts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.contacts.dto.GroupMemberEntry;

@Repository
public interface GroupMemberEntryRepository extends JpaRepository<GroupMemberEntry, Integer> {

	public List<GroupMemberEntry> findByGroupId(int groupId);

}
