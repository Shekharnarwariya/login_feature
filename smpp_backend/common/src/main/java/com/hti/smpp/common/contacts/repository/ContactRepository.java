package com.hti.smpp.common.contacts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.contacts.dto.ContactEntry;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntry, Integer> {

	public List<ContactEntry> findByGroupId(int groupId);

	public long countByGroupId(int groupId);

}