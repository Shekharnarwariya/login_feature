package com.hti.smpp.common.smsc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.smsc.dto.CustomEntry;

@Repository
public interface CustomEntryRepository extends JpaRepository<CustomEntry, Integer> {

}
