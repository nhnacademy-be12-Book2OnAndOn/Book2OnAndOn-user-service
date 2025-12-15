package com.example.book2onandonuserservice.address.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressResponseDto {
    private Long addressId;
    private String userAddressName;
    private String recipient;
    private String phone;
    private String zipCode;
    private String userAddress;
    private String userAddressDetail;
    private boolean isDefault;
}
