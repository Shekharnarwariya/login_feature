package com.hti.smpp.common.alert.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.alert.dto.AlertEntity;


@Repository
public interface AlertRespository extends JpaRepository<AlertEntity,Integer> {

}
