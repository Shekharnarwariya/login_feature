package com.hti.smpp.common.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.OTPEntry;

@Repository
public interface OtpEntryRepository extends JpaRepository<OTPEntry, Integer> {

	public Optional<OTPEntry> findBySystemId(String username);

}
