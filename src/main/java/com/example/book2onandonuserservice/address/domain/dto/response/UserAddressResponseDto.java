package com.example.book2onandonuserservice.address.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
public class UserAddressResponseDto {
    private Long addressId;
    private String userAddressName;
    private String userAddress;
    private String userAddressDetail;
    private boolean isDefault;

    @Builder
    public UserAddressResponseDto(Long addressId, String userAddressName, String userAddress,
                                  String userAddressDetail, boolean isDefault) {
        this.addressId = addressId;
        this.userAddressName = userAddressName;
        this.userAddress = userAddress;
        this.userAddressDetail = userAddressDetail;
        this.isDefault = isDefault;
    }
}
