
package com.hti.smpp.common.contacts.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.contacts.dto.GroupDataEntry;

@Repository
public interface GroupDataEntryRepository extends JpaRepository<GroupDataEntry, Integer>{

	public List<GroupDataEntry> findByGroupId(int groupId);

	public long countByGroupId(int groupId);
	
	public List<GroupDataEntry> findByNumberInAndGroupId(Long[] numbers, Integer groupId);
	
	Page<GroupDataEntry> findByGroupIdOrderByIdAsc(int groupId, PageRequest pageRequest);
	
	@Query(value = "SELECT DISTINCT profession FROM groupcontacts", nativeQuery = true)
    List<String> findDistinctProfessions();
	
	@Query(value = "SELECT DISTINCT company FROM groupcontacts", nativeQuery = true)
	List<String> findDistinctCompany();
	
	@Query(value = "SELECT DISTINCT area FROM groupcontacts", nativeQuery = true)
	List<String> findDistinctArea();

}