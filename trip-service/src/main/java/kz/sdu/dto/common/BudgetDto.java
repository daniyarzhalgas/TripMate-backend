package kz.sdu.dto.common;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetDto {

    private BigDecimal amount;
    private String currency;
}
