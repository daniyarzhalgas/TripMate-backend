package kz.sdu.repository;

import kz.sdu.entity.TripRequestParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRequestParticipantRepository extends JpaRepository<TripRequestParticipant, UUID> {

    List<TripRequestParticipant> findByTripRequestIdOrderByAddedAtAsc(UUID tripRequestId);

    Optional<TripRequestParticipant> findByTripRequestIdAndUserId(UUID tripRequestId, UUID userId);

    boolean existsByTripRequestIdAndUserId(UUID tripRequestId, UUID userId);

    void deleteByTripRequestIdAndUserId(UUID tripRequestId, UUID userId);
}
