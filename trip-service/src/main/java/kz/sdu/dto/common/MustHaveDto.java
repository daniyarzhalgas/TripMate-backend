package kz.sdu.dto.common;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MustHaveDto {

    private AgeRangeDto ageRange;

    private List<String> gender;

    private Boolean verifiedOnly;
}
