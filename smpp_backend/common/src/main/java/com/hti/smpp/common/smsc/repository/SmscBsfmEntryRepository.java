package com.hti.smpp.common.smsc.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.hti.smpp.common.smsc.dto.SmscBsfmEntry;

public interface SmscBsfmEntryRepository extends JpaRepository<SmscBsfmEntry, Integer> {

}
