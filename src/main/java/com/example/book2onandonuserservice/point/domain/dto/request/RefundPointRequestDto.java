package com.example.book2onandonuserservice.point.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundPointRequestDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;
    @NotNull
    private Long orderId;
    @NotNull
    private Long returnId;
    private Integer usedPoint;
    private Integer returnAmount;
}
