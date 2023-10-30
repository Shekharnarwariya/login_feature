package com.hti.smpp.common.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.BalanceEntry;

@Repository

public interface BalanceEntryRepository extends JpaRepository<BalanceEntry, Integer> {

	public Optional<BalanceEntry> findBySystemId(String system_id);

	public Optional<BalanceEntry> findByUserId(int valueOf);

}
