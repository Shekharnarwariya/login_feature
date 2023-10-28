package com.hti.smpp.common.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.UserEntry;

@Repository
public interface UserEntryRepository extends JpaRepository<UserEntry, Integer> {

	public Optional<UserEntry> findBySystemId(String system_id);

}
