package com.hti.smpp.common.mobileDb.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hti.smpp.common.mobileDb.dto.MobileDbEntity;

@Repository
public interface MobileDbRepo extends JpaRepository<MobileDbEntity , Integer> {

	ArrayList<MobileDbEntity> findByMobileNumber(String mobNumber);
	
//	@Query("SELECT m FROM MobileDbEntity m WHERE m.mobileNumber = :mobNumber")
//    MobileDbEntity findCustomNameForSingle(@Param("mobNumber") String mobNumber);
	
	@Query("SELECT DISTINCT m.profession FROM MobileDbEntity m")
	ArrayList<String> findDistinctByProfession();
	
	@Query("SELECT DISTINCT m.area FROM MobileDbEntity m")
	ArrayList<String> findDistinctByArea();
	
	@Query("SELECT DISTINCT m.subArea FROM MobileDbEntity m WHERE :areas IS NULL OR m.area IN :areas")
    ArrayList<String> findDistinctSubareaByAreaIn(@Param("areas") String areas);
	
	
	
	
	
	
	
	
	
	
	
	 @Modifying
	 @Transactional
	 @Query("DELETE FROM MobileDbEntity e WHERE e.mobileNumber = :mobileNumber")
	 int deleteByMobileNumber(@Param("mobileNumber") String mobileNumber);
	 
	 
	 @Modifying
	 @Transactional
	 @Query("UPDATE MobileDbEntity m SET m.sex = :sex, m.age = :age, m.vip = :vip, m.area = :area, m.classType = :classType, m.profession = :profession, m.subArea = :subArea WHERE m.mobileNumber = :mobileNumber")
	    void updateByMobileNumber(
	    		@Param("mobileNumber") String mobileNumber,
	            @Param("sex") String sex,
	            @Param("age") int age,
	            @Param("vip") String vip,
	            @Param("area") String area,
	            @Param("classType") String classType,
	            @Param("profession") String profession,
	            @Param("subArea") String subArea
	    );
	
}
