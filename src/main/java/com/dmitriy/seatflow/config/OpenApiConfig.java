package com.dmitriy.seatflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Настраивает общие метаданные документации SeatFlow API.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Создаёт основное описание API, отображаемое в Swagger UI.
     */
    @Bean
    public OpenAPI seatFlowOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SeatFlow API")
                        .description("REST API for event venue and ticket booking platform")
                        .version("v1"));
    }
}