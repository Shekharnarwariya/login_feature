package com.hti.smpp.common.network.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.network.dto.NetworkEntry;

@Repository
public interface NetworkEntryRepository extends JpaRepository<NetworkEntry, Integer> {

	@Query(value = "SELECT DISTINCT n.mcc, n.country FROM NetworkEntry n ORDER BY n.country")
	List<Object[]> findDistinctCountries();

	List<NetworkEntry> findByMcc(String mcc);

	@Query("SELECT n.id FROM NetworkEntry n WHERE n.mcc = :mcc AND n.mnc IN :mncList")
	List<Integer> checkDuplicateMccMnc(@Param("mcc") String mcc, @Param("mncList") List<String> mncList);

	@Modifying
	@Query("DELETE FROM NetworkEntry n WHERE n.id IN :ids")
	void deleteByIdIn(@Param("ids") List<Integer> ids);

	@Query(value = "SELECT * FROM network n WHERE n.cc LIKE :cc AND n.mcc LIKE :mcc AND n.mnc LIKE :mnc ORDER BY n.cc", nativeQuery = true)
	List<NetworkEntry> findByCcAndMccAndMnc(@Param("cc") String cc, @Param("mcc") String mcc, @Param("mnc") String mnc);

	@Query(value = "SELECT * FROM network n WHERE n.cc LIKE :cc AND n.mcc LIKE :mcc AND n.mnc LIKE :mnc ORDER BY n.cc", nativeQuery = true)
	Page<NetworkEntry> findByPaginated(@Param("cc") String cc, @Param("mcc") String mcc, @Param("mnc") String mnc,
			Pageable pageable);

	@Query(value = "SELECT DISTINCT n.mcc FROM network n WHERE n.cc LIKE :cc", nativeQuery = true)
	public List<String> findDistinctMCCByCc(String cc);

	@Query("SELECT DISTINCT n.mnc, n.operator FROM NetworkEntry n WHERE n.mcc LIKE :MCC")
	List<Object[]> findDistinctMNCAndOperatorByMCCLike(String MCC);

}
