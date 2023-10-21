package com.hti.smpp.common.hlr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.hlr.dto.HlrSmscEntry;

@Repository
public interface HlrSmscRepository extends JpaRepository<HlrSmscEntry, Integer> {

}