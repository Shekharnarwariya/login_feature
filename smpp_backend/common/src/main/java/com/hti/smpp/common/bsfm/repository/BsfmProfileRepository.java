package com.hti.smpp.common.bsfm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.bsfm.dto.Bsfm;

@Repository
public interface BsfmProfileRepository extends JpaRepository<Bsfm, Integer> {
}