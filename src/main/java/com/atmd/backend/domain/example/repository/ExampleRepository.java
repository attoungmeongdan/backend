package com.atmd.backend.domain.example.repository;

import com.atmd.backend.domain.example.entity.Example;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleRepository extends JpaRepository<Example, Long> {
}
