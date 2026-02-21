package kz.sdu.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Участник заявки на поездку.
 * Владелец заявки (TripRequest.userId) может добавлять других пользователей в заявку.
 */
@Entity
@Table(name = "trip_request_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"trip_request_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripRequestParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "trip_request_id", nullable = false)
    private UUID tripRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_request_id", insertable = false, updatable = false)
    private TripRequest tripRequest;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
