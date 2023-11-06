package com.hti.smpp.common.hlr.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.hlr.dto.HlrSmscEntry;

@Repository
public interface HlrSmscRepository extends JpaRepository<HlrSmscEntry, Integer> {

	public Optional<HlrSmscEntry> findByIdAndSystemId(int id, String system_id);

	public List<HlrSmscEntry> findBySystemId(String system_id);

}