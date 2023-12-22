package com.hti.smpp.common.templates.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.templates.dto.TemplatesDTO;

@Repository
public interface TemplatesRepository extends JpaRepository<TemplatesDTO, Integer> {

	public Optional<TemplatesDTO> findByIdAndMasterId(int id, String system_id);

	public List<TemplatesDTO> findByMasterId(String system_id);

	@Modifying
	@Query("DELETE FROM TemplatesDTO t WHERE t.id = :id AND t.masterId = :masterId")
	public void deleteByIdAndMasterId(@Param("id") int id, @Param("masterId") String masterId);

}
