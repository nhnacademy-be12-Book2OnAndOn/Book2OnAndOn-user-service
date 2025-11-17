package com.example.book2onandonuserservice.addres.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class UserAddressCreateRequest {
    @Size(max = 20)
    private String userAddressName;

    @NotNull
    @Size(min = 1, max = 100)
    private String userAddress;

    @Size(max = 100)
    private String userAddressDetail;

}
