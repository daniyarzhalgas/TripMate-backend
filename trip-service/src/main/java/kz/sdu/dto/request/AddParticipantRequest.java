package kz.sdu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddParticipantRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;
}
