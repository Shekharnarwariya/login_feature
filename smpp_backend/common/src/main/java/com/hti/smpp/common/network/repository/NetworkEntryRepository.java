package com.hti.smpp.common.network.repository;

<<<<<<< HEAD
import java.util.Set;
=======
import java.util.List;
>>>>>>> c1c9d3293b31081b1a4e5da3d6c18eae3f89af92

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.network.dto.NetworkEntry;

@Repository
public interface NetworkEntryRepository extends JpaRepository<NetworkEntry, Integer> {

<<<<<<< HEAD
	static Set<Integer> listExistNetwork(int i) {
		// TODO Auto-generated method stub
		return null;
	}
=======
	@Query(value = "SELECT DISTINCT n.mcc, n.country FROM NetworkEntry n ORDER BY n.country", nativeQuery = true)
	List<Object[]> findDistinctCountries();
>>>>>>> c1c9d3293b31081b1a4e5da3d6c18eae3f89af92

}
