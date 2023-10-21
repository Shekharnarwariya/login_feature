package com.hti.smpp.common.network.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.network.dto.NetworkEntry;

@Repository
public interface NetworkEntryRepository extends JpaRepository<NetworkEntry, String> {

}
