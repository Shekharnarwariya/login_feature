package com.hti.smpp.common.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.MultiUserEntry;

@Repository
public interface MultiUserEntryRepository extends JpaRepository<MultiUserEntry, Integer> {

	public MultiUserEntry findByUserId(int intValue);

}
