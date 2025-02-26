
package com.hti.smpp.common.contacts.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.contacts.dto.GroupDataEntry;

@Repository
public interface GroupDataEntryRepository extends JpaRepository<GroupDataEntry, Integer>{

	public List<GroupDataEntry> findByGroupId(int groupId);
	
	@Query("SELECT g FROM GroupDataEntry g WHERE g.createdOn BETWEEN :start AND :end")
	List<GroupDataEntry> findGroupDataByDate(@Param("start") String start, @Param("end") String end);
	
	public long countByGroupId(int groupId);
	
	public List<GroupDataEntry> findByNumberInAndGroupId(Long[] numbers, Integer groupId);
	
	public List<GroupDataEntry> findByGroupIdOrderByIdAsc(int groupId);
	
	@Query(value = "SELECT DISTINCT profession FROM groupcontacts", nativeQuery = true)
    List<String> findDistinctProfessions();
	
	@Query(value = "SELECT DISTINCT company FROM groupcontacts", nativeQuery = true)
	List<String> findDistinctCompany();
	
	@Query(value = "SELECT DISTINCT area FROM groupcontacts", nativeQuery = true)
	List<String> findDistinctArea();

	public List<GroupDataEntry> findByGroupId(int groupId, PageRequest pageRequest);
	

}