package com.hti.smpp.common.contacts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.contacts.dto.ContactEntry;

@Repository
public interface ContactRepository extends JpaRepository<ContactEntry, Integer> {

	public List<ContactEntry> findByGroupId(int groupId);

//	@Query("SELECT c FROM ContactEntry c WHERE c.createdOn BETWEEN :start AND :end")
//	List<ContactEntry> findContactByDate(@Param("start") String start, @Param("end") String end);

	public long countByGroupId(int groupId);

	@Query("SELECT c FROM ContactEntry c WHERE c.groupId = :groupId AND c.createdOn BETWEEN :start AND :end")
	List<ContactEntry> findContactByDateAndGroupId(@Param("start") String start, @Param("end") String end,
			@Param("groupId") int groupId);

}