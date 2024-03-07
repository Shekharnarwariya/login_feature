package com.hti.smpp.common.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.RechargeEntry;

@Repository
public interface RechargeEntryRepository extends JpaRepository<RechargeEntry, Integer> {

	@Query("SELECT r FROM RechargeEntry r WHERE r.userId IN :userIds AND r.particular LIKE :particularPattern AND r.time BETWEEN :startTime AND :endTime")
	List<RechargeEntry> findByCriteria(@Param("userIds") List<Integer> userIds,
			@Param("particularPattern") String particularPattern, @Param("startTime") String startTime,
			@Param("endTime") String endTime);
}
