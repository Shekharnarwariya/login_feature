package com.hti.smpp.common.messages.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.messages.dto.BulkEntry;

@Repository
public interface BulkEntryRepository extends JpaRepository<BulkEntry, Integer> {

}
