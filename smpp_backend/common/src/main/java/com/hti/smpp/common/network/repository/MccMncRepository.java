package com.hti.smpp.common.network.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.network.dto.MccMncDTO;

@Repository
public interface MccMncRepository extends JpaRepository<MccMncDTO, Integer> {

}
