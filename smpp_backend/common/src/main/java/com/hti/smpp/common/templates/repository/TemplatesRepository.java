package com.hti.smpp.common.templates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.templates.dto.TemplatesDTO;

@Repository
public interface TemplatesRepository extends JpaRepository<TemplatesDTO, Integer> {

}
