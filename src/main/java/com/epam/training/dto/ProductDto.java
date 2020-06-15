package com.epam.training.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    @NotNull
    @NotEmpty(message = "Product's name must be not empty")
    private String name;
    private Integer id;
    private Integer category1;
    private Integer category2;
    private Integer category3;
    private BigDecimal amount1;
    private BigDecimal amount2;
    private BigDecimal amount3;
}
