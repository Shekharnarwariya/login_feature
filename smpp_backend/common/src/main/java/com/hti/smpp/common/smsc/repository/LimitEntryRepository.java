package com.hti.smpp.common.smsc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hti.smpp.common.smsc.dto.LimitEntry;

public interface LimitEntryRepository extends JpaRepository<LimitEntry, Integer> {

}
