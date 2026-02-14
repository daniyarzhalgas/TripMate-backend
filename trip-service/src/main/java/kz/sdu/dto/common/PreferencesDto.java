package kz.sdu.dto.common;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreferencesDto {

    @Valid
    private MustHaveDto mustHave;

    @Valid
    private NiceToHaveDto niceToHave;
}
