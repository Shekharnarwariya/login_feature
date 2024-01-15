package com.hti.smpp.common.messages.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.messages.dto.BulkMapEntry;

@Repository
public interface BulkContentEntryRespository extends JpaRepository<BulkMapEntry, Long> {

}