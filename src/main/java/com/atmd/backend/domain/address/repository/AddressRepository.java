package com.atmd.backend.domain.address.repository;

import com.atmd.backend.domain.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
