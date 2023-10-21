package com.hti.smpp.common.config.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.config.dto.DltTemplEntry;

@Repository
public interface DltTemplEntryRepository extends JpaRepository<DltTemplEntry, Integer> {

}
