package com.hti.smpp.common.smsc.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.smsc.dto.SmscLooping;

@Repository
public interface SmscLoopingRepository extends JpaRepository<SmscLooping, Integer> {

	public Optional<SmscLooping> findBySmscId(long smscId);

}
