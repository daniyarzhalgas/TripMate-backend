package kz.sdu.dto.common;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgeRangeDto {

    private Integer min;
    private Integer max;
}

