package com.projbase.api.domain.example.service;

import com.projbase.api.domain.example.dto.request.ExampleRequestDTO;
import com.projbase.api.domain.example.dto.response.ExampleResponseDTO;
import com.projbase.api.domain.example.entity.Example;
import com.projbase.api.domain.example.exception.ExampleErrorCode;
import com.projbase.api.domain.example.repository.ExampleRepository;
import com.projbase.api.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;

    @Transactional
    public ExampleResponseDTO create(ExampleRequestDTO request) {
        Example example = Example.builder()
                .name(request.getName())
                .build();
        return ExampleResponseDTO.from(exampleRepository.save(example));
    }

    @Transactional(readOnly = true)
    public ExampleResponseDTO get(Long id) {
        Example example = exampleRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ExampleErrorCode.EXAMPLE_NOT_FOUND));
        return ExampleResponseDTO.from(example);
    }
}
