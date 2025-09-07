package com.example.carins.repo;

import com.example.carins.model.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long> {
    
    List<InsuranceClaim> findByCarId(Long carId);
    
    @Query("SELECT c FROM InsuranceClaim c WHERE c.car.id = :carId ORDER BY c.claimDate")
    List<InsuranceClaim> findByCarIdOrderByClaimDate(@Param("carId") Long carId);

}