package com.hti.smpp.common.config.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.config.dto.DltEntry;

@Repository
public interface DltEntryRepository extends JpaRepository<DltEntry, Integer> {

}
