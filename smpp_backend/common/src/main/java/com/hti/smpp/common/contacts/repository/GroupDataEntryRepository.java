
package com.hti.smpp.common.contacts.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.contacts.dto.GroupDataEntry;

@Repository
public interface GroupDataEntryRepository extends JpaRepository<GroupDataEntry, Integer> {
	
	public List<GroupDataEntry> findByGroupIdAndProfessionInAndCompanyInAndAreaInAndGenderInAndNumberInAndAgeBetween(
	        int groupId, List<String> profession, List<String> company, List<String> area,
	        List<String> gender, List<Long> number, int minAge, int maxAge);
	

}