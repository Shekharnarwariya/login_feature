package com.hti.smpp.common.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hti.smpp.common.management.dto.BulkMgmtEntry;

public interface BulkMgmtEntryRepository extends JpaRepository<BulkMgmtEntry, Integer> {

}
