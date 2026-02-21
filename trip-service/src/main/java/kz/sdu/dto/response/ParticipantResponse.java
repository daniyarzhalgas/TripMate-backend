package kz.sdu.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ParticipantResponse {

    private UUID id;
    private UUID userId;
    private OffsetDateTime addedAt;
}
