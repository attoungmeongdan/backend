package com.atmd.backend.domain.facility.repository;

import com.atmd.backend.domain.facility.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    @Query(value = """
            SELECT *
            FROM facilities
            WHERE is_deleted = false
              AND (6371 * acos(LEAST(1.0,
                      cos(radians(:userLat)) * cos(radians(lat)) * cos(radians(lng) - radians(:userLng))
                      + sin(radians(:userLat)) * sin(radians(lat))
                  ))) <= 5
            """, nativeQuery = true)
    List<Facility> findWithinRadius(@Param("userLat") Double userLat, @Param("userLng") Double userLng);
}
