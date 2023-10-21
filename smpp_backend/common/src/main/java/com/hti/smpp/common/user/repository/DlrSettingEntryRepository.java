package com.hti.smpp.common.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.user.dto.DlrSettingEntry;

@Repository
public interface DlrSettingEntryRepository extends JpaRepository<DlrSettingEntry, Integer> {

}
