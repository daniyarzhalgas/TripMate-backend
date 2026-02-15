package kz.sdu.controller;

import kz.sdu.dto.ApiResponse;
import kz.sdu.dto.request.CreateTripRequestRequest;
import kz.sdu.dto.request.UpdateTripRequestRequest;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/trip-requests")
@RequiredArgsConstructor
public class TripController {

    private final TripRequestService tripRequestService;

    /**
     * 3.1 Create Trip Request
     */

    @GetMapping("/test")
    public String test() {
        return "hello world";
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TripRequestResponse>> createTripRequest(
            Authentication authentication,
            @Valid @RequestBody CreateTripRequestRequest request
    ) {
        UUID userId = userIdFrom(authentication);
        TripRequestResponse response =
                tripRequestService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 3.2 Get My Trip Requests
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TripRequestPageResponse>> getMyTripRequests(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        UUID userId = userIdFrom(authentication);
        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<TripRequestShortResponse> requests =
                tripRequestService.getMyRequests(userId, status, pageable);

        TripRequestPageResponse response =
                TripRequestPageResponse.from(requests);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
    

    /**
     * 3.3 Get Trip Request by ID
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<TripRequestResponse>> getById(
            Authentication authentication,
            @PathVariable UUID requestId
    ) {
        UUID userId = userIdFrom(authentication);
        TripRequestResponse response =
                tripRequestService.getById(userId, requestId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 3.4 Update Trip Request
     */
    @PutMapping("/{requestId}")
    public ResponseEntity<ApiResponse<TripRequestUpdateResponse>> update(
            Authentication authentication,
            @PathVariable UUID requestId,
            @RequestBody UpdateTripRequestRequest request
    ) {
        UUID userId = userIdFrom(authentication);
        TripRequestUpdateResponse response =
                tripRequestService.update(userId, requestId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 3.5 Delete Trip Request
     */
    @DeleteMapping("/{requestId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication authentication,
            @PathVariable UUID requestId
    ) {
        UUID userId = userIdFrom(authentication);
        tripRequestService.delete(userId, requestId);

        return ResponseEntity.ok(
                ApiResponse.successMessage("Trip request deleted successfully")
        );
    }

    private static UUID userIdFrom(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AuthenticationCredentialsNotFoundException("Not authenticated");
        }
        return UUID.fromString(authentication.getName());
    }
}
