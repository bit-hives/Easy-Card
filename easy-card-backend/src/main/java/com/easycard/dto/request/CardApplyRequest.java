package com.easycard.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardApplyRequest {
    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @NotNull(message = "Credit limit is required")
    @DecimalMin(value = "100.0", message = "Credit limit must be at least 100")
    private BigDecimal creditLimit;
}
