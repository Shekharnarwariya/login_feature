package com.hti.smpp.common.network.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.network.dto.NetworkEntry;

@Repository
public interface NetworkEntryRepository extends JpaRepository<NetworkEntry, Integer> {

	@Query(value = "SELECT DISTINCT n.mcc, n.country FROM NetworkEntry n ORDER BY n.country", nativeQuery = true)
	List<Object[]> findDistinctCountries();
	
	@Query("SELECT n.id FROM NetworkEntry n WHERE n.mcc = :mcc AND n.mnc IN :mncList")
    List<Integer> checkDuplicateMccMnc(@Param("mcc") String mcc, @Param("mncList") List<Integer> mncList);
	
	@Modifying
    @Query("DELETE FROM NetworkEntry n WHERE n.id IN :ids")
    void deleteByIdIn(@Param("ids") List<Integer> ids);
	
	@Query("SELECT n FROM NetworkEntry n WHERE n.cc LIKE %:cc% AND n.mcc LIKE %:mcc% AND n.mnc LIKE %:mnc% ORDER BY n.cc")
    List<NetworkEntry> findByCcAndMccAndMnc(@Param("cc") String cc, @Param("mcc") String mcc, @Param("mnc") String mnc);
	
	@Query("SELECT DISTINCT n.mcc FROM NetworkEntry n WHERE n.cc LIKE %:cc%")
    List<String> findDistinctMCCByCc(String cc);
	
	@Query("SELECT DISTINCT n.mnc, n.operator FROM NetworkEntry n WHERE n.mnc LIKE %:MCC%")
	List<Object[]> findDistinctMNCAndOperatorByMCCLike(String MCC);

}
