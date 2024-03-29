package com.hti.smpp.common.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.MultiUserEntry;

@Repository
public interface MultiUserEntryRepository extends JpaRepository<MultiUserEntry, Integer> {

	public MultiUserEntry findByUserId(int intValue);

	@Query(value = "SELECT * FROM multi_user_access WHERE user_id = :userId", nativeQuery = true)
	public List<MultiUserEntry> findByUserNative(@Param("userId") int userId);

	public List<MultiUserEntry> findByUserIdEquals(int userId);

}
