package com.hti.smpp.common.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.WebMasterEntry;

@Repository
public interface WebMasterEntryRepository extends JpaRepository<WebMasterEntry, Integer> {

	public WebMasterEntry findByUserId(int system_id);

}
