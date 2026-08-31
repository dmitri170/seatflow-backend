package com.dmitriy.seatflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    void shouldProvideSeatFlowApiMetadata() {
        OpenAPI openAPI = openApiConfig.seatFlowOpenApi();

        assertNotNull(openAPI.getInfo());

        assertAll(
                () -> assertEquals("SeatFlow API", openAPI.getInfo().getTitle()),
                () -> assertEquals(
                        "REST API for event venue and ticket booking platform",
                        openAPI.getInfo().getDescription()
                ),
                () -> assertEquals("v1", openAPI.getInfo().getVersion())
        );
    }
}