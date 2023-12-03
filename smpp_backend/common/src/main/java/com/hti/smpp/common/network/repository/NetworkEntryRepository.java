package com.hti.smpp.common.network.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.network.dto.NetworkEntry;

@Repository
public interface NetworkEntryRepository extends JpaRepository<NetworkEntry, Integer> {

	@Query(value = "SELECT DISTINCT n.mcc, n.country FROM NetworkEntry n ORDER BY n.country", nativeQuery = true)
	List<Object[]> findDistinctCountries();

}
