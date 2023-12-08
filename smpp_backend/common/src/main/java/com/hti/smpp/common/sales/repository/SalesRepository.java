package com.hti.smpp.common.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.sales.dto.SalesEntry;

@Repository
public interface SalesRepository extends JpaRepository<SalesEntry, Integer> {

	public SalesEntry findByMasterId(String systemId);

	public List<SalesEntry> findByMasterIdAndRole(String mgrId, String role);

}