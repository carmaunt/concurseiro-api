package br.com.concurseiro.api.infra;

import java.time.OffsetDateTime;

public record ApiResponse<T>(
        boolean success,
        T data,
        OffsetDateTime timestamp,
        String path
) {
    public static <T> ApiResponse<T> success(T data, String path) {
        return new ApiResponse<>(
                true,
                data,
                OffsetDateTime.now(),
                path
        );
    }
}