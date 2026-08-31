package com.dmitriy.seatflow.sector;

import com.dmitriy.seatflow.sector.dto.CreateSectorRequest;
import com.dmitriy.seatflow.sector.dto.SectorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для работы с секторами залов.
 */
/**
 * REST-контроллер для работы с секторами залов.
 */
@Tag(
        name = "Sectors",
        description = "Операции для управления секторами залов"
)
@RestController
@RequestMapping("/api/v1")
public class SectorController {

    private final SectorService sectorService;

    public SectorController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    /**
     * Создает новый сектор в указанном зале.
     *
     * @param hallId идентификатор зала
     * @param request данные для создания сектора
     * @return созданный сектор
     */
    @Operation(
            summary = "Создать сектор",
            description = "Создает новый сектор в указанном зале и автоматически генерирует места"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Сектор успешно создан"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Зал не найден"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Сектор с таким именем уже существует в зале"
            )
    })
    @PostMapping("/halls/{hallId}/sectors")
    public ResponseEntity<SectorResponse> createSector(
            @PathVariable UUID hallId,
            @Valid @RequestBody CreateSectorRequest request
    ) {
        SectorResponse response = sectorService.createSector(hallId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Возвращает все сектора указанного зала.
     *
     * @param hallId идентификатор зала
     * @return список секторов
     */
    @Operation(
            summary = "Получить сектора зала",
            description = "Возвращает список всех секторов указанного зала"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список секторов успешно получен"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Зал не найден"
            )
    })
    @GetMapping("/halls/{hallId}/sectors")
    public List<SectorResponse> getSectorsByHallId(
            @PathVariable UUID hallId
    ) {
        return sectorService.getSectorsByHallId(hallId);
    }
}