
package com.hti.smpp.common.contacts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.contacts.dto.GroupDataEntry;

@Repository
public interface GroupDataEntryRepository extends JpaRepository<GroupDataEntry, Integer> {

}