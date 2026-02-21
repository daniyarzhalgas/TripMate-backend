package kz.sdu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.sdu.dto.ApiResponseDto;
import kz.sdu.dto.request.AddParticipantRequest;
import kz.sdu.dto.request.CreateTripRequestRequest;
import kz.sdu.dto.request.UpdateTripRequestRequest;
import kz.sdu.dto.response.ParticipantResponse;
import kz.sdu.dto.response.TripRequestPageResponse;
import kz.sdu.dto.response.TripRequestResponse;
import kz.sdu.dto.response.TripRequestShortResponse;
import kz.sdu.dto.response.TripRequestUpdateResponse;
import kz.sdu.service.TripRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@Tag(name = "Trip Requests", description = "Заявки на поездки (создание, просмотр, обновление, удаление)")
@RestController
@RequestMapping("/api/trip-requests")
@RequiredArgsConstructor
public class TripController {

    private final TripRequestService tripRequestService;

    @Operation(summary = "Тестовый эндпоинт", description = "Проверка доступности сервиса")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/test")
    public String test() {
        return "hello world";
    }

    @Operation(summary = "Создать заявку на поездку", description = "3.1 Создание новой заявки на поездку. Требуется аутентификация.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Заявка создана"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PostMapping
    public ResponseEntity<ApiResponseDto<TripRequestResponse>> createTripRequest(
            Authentication authentication,
            @Valid @RequestBody CreateTripRequestRequest request
    ) {
        UUID userId = userIdFrom(authentication);
        TripRequestResponse response =
                tripRequestService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(response));
    }

    @Operation(summary = "Мои заявки на поездки", description = "3.2 Список заявок текущего пользователя с пагинацией и фильтром по статусу.")
    @ApiResponse(responseCode = "200", description = "Страница заявок")
    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<TripRequestPageResponse>> getMyTripRequests(
            Authentication authentication,
            @Parameter(description = "Фильтр по статусу") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Номер страницы (с 1)") @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "Размер страницы") @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        UUID userId = userIdFrom(authentication);
        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<TripRequestShortResponse> requests =
                tripRequestService.getMyRequests(userId, status, pageable);

        TripRequestPageResponse response =
                TripRequestPageResponse.from(requests);

        return ResponseEntity.ok(ApiResponseDto.success(response));
    }


    @Operation(summary = "Заявка по ID", description = "3.3 Получить заявку на поездку по её ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заявка найдена"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponseDto<TripRequestResponse>> getById(
            Authentication authentication,
            @Parameter(description = "UUID заявки") @PathVariable("requestId") UUID requestId
    ) {
        UUID userId = userIdFrom(authentication);
        TripRequestResponse response =
                tripRequestService.getById(userId, requestId);

        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @Operation(summary = "Обновить заявку", description = "3.4 Обновление заявки на поездку (даты, бюджет и т.д.).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заявка обновлена"),
            @ApiResponse(responseCode = "403", description = "Нет прав на изменение"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    @PutMapping("/{requestId}")
    public ResponseEntity<ApiResponseDto<TripRequestUpdateResponse>> update(
            Authentication authentication,
            @Parameter(description = "UUID заявки") @PathVariable("requestId") UUID requestId,
            @RequestBody UpdateTripRequestRequest request
    ) {
        UUID userId = userIdFrom(authentication);
        TripRequestUpdateResponse response =
                tripRequestService.update(userId, requestId, request);

        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @Operation(summary = "Удалить заявку", description = "3.5 Удаление заявки на поездку. Доступно только владельцу.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Заявка удалена"),
            @ApiResponse(responseCode = "403", description = "Нет прав"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            Authentication authentication,
            @Parameter(description = "UUID заявки") @PathVariable("requestId") UUID requestId
    ) {
        UUID userId = userIdFrom(authentication);
        tripRequestService.delete(userId, requestId);

        return ResponseEntity.ok(
                ApiResponseDto.successMessage("Trip request deleted successfully")
        );
    }

    @Operation(summary = "Добавить участника", description = "Владелец заявки добавляет пользователя в участники заявки на поездку.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Участник добавлен"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос (уже участник, попытка добавить владельца)"),
            @ApiResponse(responseCode = "403", description = "Только владелец может добавлять участников"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    @PostMapping("/{requestId}/participants")
    public ResponseEntity<ApiResponseDto<ParticipantResponse>> addParticipant(
            Authentication authentication,
            @Parameter(description = "UUID заявки") @PathVariable("requestId") UUID requestId,
            @Valid @RequestBody AddParticipantRequest request
    ) {
        UUID userId = userIdFrom(authentication);
        ParticipantResponse response = tripRequestService.addParticipant(userId, requestId, request);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @Operation(summary = "Удалить участника", description = "Владелец заявки удаляет пользователя из участников.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Участник удалён"),
            @ApiResponse(responseCode = "403", description = "Только владелец может удалять участников"),
            @ApiResponse(responseCode = "404", description = "Заявка или участник не найдены")
    })
    @DeleteMapping("/{requestId}/participants/{userId}")
    public ResponseEntity<ApiResponseDto<Void>> removeParticipant(
            Authentication authentication,
            @Parameter(description = "UUID заявки") @PathVariable("requestId") UUID requestId,
            @Parameter(description = "UUID удаляемого участника") @PathVariable("userId") UUID participantUserId
    ) {
        UUID ownerUserId = userIdFrom(authentication);
        tripRequestService.removeParticipant(ownerUserId, requestId, participantUserId);
        return ResponseEntity.ok(ApiResponseDto.successMessage("Participant removed"));
    }

    @Operation(summary = "Список участников", description = "Список участников заявки. Доступен владельцу заявки и самим участникам.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список участников"),
            @ApiResponse(responseCode = "403", description = "Нет доступа"),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    @GetMapping("/{requestId}/participants")
    public ResponseEntity<ApiResponseDto<List<ParticipantResponse>>> getParticipants(
            Authentication authentication,
            @Parameter(description = "UUID заявки") @PathVariable("requestId") UUID requestId
    ) {
        UUID userId = userIdFrom(authentication);
        List<ParticipantResponse> list = tripRequestService.getParticipants(userId, requestId);
        return ResponseEntity.ok(ApiResponseDto.success(list));
    }

    private static UUID userIdFrom(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AuthenticationCredentialsNotFoundException("Not authenticated");
        }
        return UUID.fromString(authentication.getName());
    }
}
