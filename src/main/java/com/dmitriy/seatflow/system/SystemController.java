package com.dmitriy.seatflow.system;

import com.dmitriy.seatflow.common.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "System",
        description = "System availability and service status"
)
@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @Operation(summary = "Check service availability")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Service is available",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SystemStatusResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @GetMapping("/ping")
    public SystemStatusResponse ping() {
        return new SystemStatusResponse("UP", "seatflow-backend");
    }
}