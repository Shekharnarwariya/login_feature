package com.hti.smpp.common.util.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.util.dto.SubscribeEntry;

@Repository
public interface SubscribeEntryRepository extends JpaRepository<SubscribeEntry, Integer>  {

}
