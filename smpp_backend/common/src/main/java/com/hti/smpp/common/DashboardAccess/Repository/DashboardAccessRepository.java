package com.hti.smpp.common.DashboardAccess.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.DashboardAccess.Entity.DashboardAccess;

@Repository
public interface DashboardAccessRepository extends JpaRepository<DashboardAccess, Integer> {

	public Optional<DashboardAccess> findByUserId(int userId);

}
