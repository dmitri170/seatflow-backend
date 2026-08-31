package com.dmitriy.seatflow.seat;

import com.dmitriy.seatflow.seat.dto.SeatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для работы с местами в секторах.
 */
@Tag(
        name = "Seats",
        description = "Операции для получения мест в секторах"
)
@RestController
@RequestMapping("/api/v1")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    /**
     * Возвращает все места указанного сектора.
     *
     * @param sectorId идентификатор сектора
     * @return список мест сектора
     */
    @Operation(
            summary = "Получить места сектора",
            description = "Возвращает все места указанного сектора в порядке рядов и номеров мест"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список мест успешно получен"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сектор не найден"
            )
    })
    @GetMapping("/sectors/{sectorId}/seats")
    public List<SeatResponse> getSeatsBySectorId(
            @PathVariable UUID sectorId
    ) {
        return seatService.getSeatsBySectorId(sectorId);
    }
}