package com.hti.smpp.common.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.management.dto.BulkManagementEntity;

@Repository
public interface BulkMgmtEntryRepository extends JpaRepository<BulkManagementEntity, Integer> {

}
