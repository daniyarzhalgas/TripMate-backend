package kz.sdu.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DestinationDto {

    private String city;
    private String country;
    private String countryCode;
}
