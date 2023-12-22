package com.hti.smpp.common.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.DriverInfo;

@Repository
public interface DriverInfoRepository extends JpaRepository<DriverInfo, Integer> {

}
