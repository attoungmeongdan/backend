package com.atmd.backend.global.external.ai.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class EmbeddingDataDTO {

    private int index;
    private List<Double> embedding;
}
