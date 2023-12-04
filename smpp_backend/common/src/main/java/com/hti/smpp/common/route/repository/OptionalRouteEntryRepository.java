package com.hti.smpp.common.route.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.route.dto.OptionalRouteEntry;

@Repository
public interface OptionalRouteEntryRepository extends JpaRepository<OptionalRouteEntry, Integer> {

	public OptionalRouteEntry findByRouteId(int id);

	@Query(value = "SELECT c.networkId, c.newCost FROM CrmPrice c WHERE c.smscName = :smsc AND c.networkId IN :networks", nativeQuery = true)
	public List<Object[]> findSmscPricing(@Param("smsc") String smsc, @Param("networks") Set<String> networks);

}
