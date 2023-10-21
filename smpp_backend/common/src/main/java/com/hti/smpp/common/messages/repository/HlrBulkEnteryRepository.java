package com.hti.smpp.common.messages.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hti.smpp.common.messages.dto.HlrBulkEntry;

public interface HlrBulkEnteryRepository extends JpaRepository<HlrBulkEntry,Integer>{
    
}