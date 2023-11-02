package com.hti.smpp.common.user.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.BalanceEntry;

@Repository
public interface BalanceEntryRepository extends CrudRepository<BalanceEntry, Integer> {

	Optional<BalanceEntry> findBySystemId(String systemId);

	Optional<BalanceEntry> findByUserId(Integer userId);
}
