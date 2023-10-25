package com.pelotech.sample.domain.model;

public record DetectionEvent(
        String id,
        String source,
        String userId
) {
}
