package kz.sdu.dto.common;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NiceToHaveDto {

    private String similarInterests; // high | medium | low
    private String similarBudget;    // high | medium | low
}
