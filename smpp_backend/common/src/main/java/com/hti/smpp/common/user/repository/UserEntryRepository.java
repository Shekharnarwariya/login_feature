package com.hti.smpp.common.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.UserEntry;

@Repository
public interface UserEntryRepository extends JpaRepository<UserEntry, Integer> {

}
