package com.atmd.api.domain.example.controller;

import com.atmd.api.domain.example.dto.request.ExampleRequestDTO;
import com.atmd.api.domain.example.dto.response.ExampleResponseDTO;
import com.atmd.api.domain.example.service.ExampleService;
import com.atmd.api.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/example")
@RequiredArgsConstructor
public class ExampleController {

    private final ExampleService exampleService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExampleResponseDTO>> create(@Valid @RequestBody ExampleRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(exampleService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExampleResponseDTO>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(exampleService.get(id)));
    }
}
