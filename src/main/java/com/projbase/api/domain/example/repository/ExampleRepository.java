package com.projbase.api.domain.example.repository;

import com.projbase.api.domain.example.entity.Example;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExampleRepository extends JpaRepository<Example, Long> {
}
