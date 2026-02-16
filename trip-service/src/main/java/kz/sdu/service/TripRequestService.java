package kz.sdu.service;

import kz.sdu.dto.ApiResponseDto;
import kz.sdu.dto.request.CreateTripRequestRequest;
import kz.sdu.dto.request.UpdateTripRequestRequest;
import kz.sdu.dto.response.TripRequestResponse;
import kz.sdu.dto.response.TripRequestShortResponse;
import kz.sdu.dto.response.TripRequestUpdateResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface TripRequestService {

    TripRequestResponse create(UUID userId, CreateTripRequestRequest request);

    Page<TripRequestShortResponse> getMyRequests(UUID userId, String status, Pageable pageable);

    TripRequestResponse getById(UUID userId, UUID requestId);

    TripRequestUpdateResponse update(UUID userId, UUID requestId, UpdateTripRequestRequest request);

    void delete(UUID userId, UUID requestId);

    List<TripRequestResponse> getAllTripRequest();

    List<TripRequestResponse> getAllTripRequestWithFilters(
            String gender,
            Integer minAge,
            Integer maxAge,
            java.math.BigDecimal minBudget,
            String city,
            String country,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    );
}
