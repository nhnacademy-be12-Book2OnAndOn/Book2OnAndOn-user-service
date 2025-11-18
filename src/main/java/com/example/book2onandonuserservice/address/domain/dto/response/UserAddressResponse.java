package com.example.book2onandonuserservice.address.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
public class UserAddressResponse {
    private Long addressId;
    private String userAddressName;
    private String userAddress;
    private String userAddressDetail;

    @Builder
    public UserAddressResponse(Long addressId, String userAddressName, String userAddress, String userAddressDetail) {
        this.addressId = addressId;
        this.userAddressName = userAddressName;
        this.userAddress = userAddress;
        this.userAddressDetail = userAddressDetail;
    }
}
