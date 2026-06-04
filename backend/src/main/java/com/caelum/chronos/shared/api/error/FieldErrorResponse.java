package com.caelum.chronos.shared.api.error;

public record FieldErrorResponse(
        String field,
        String message) {
}