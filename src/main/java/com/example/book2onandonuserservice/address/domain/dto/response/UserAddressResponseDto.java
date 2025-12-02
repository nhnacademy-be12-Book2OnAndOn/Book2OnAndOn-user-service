package com.example.book2onandonuserservice.address.domain.dto.response;

import com.example.book2onandonuserservice.address.domain.entity.Address;
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

    public static UserAddressResponseDto fromEntity(Address address) {
        return UserAddressResponseDto.builder()
                .addressId(address.getAddressId())
                .userAddressName(address.getUserAddressName())
                .recipient(address.getRecipient())
                .phone(address.getPhone())
                .zipCode(address.getZipCode())
                .userAddress(address.getUserAddress())
                .userAddressDetail(address.getUserAddressDetail())
                .isDefault(address.isDefault())
                .build();
    }
}
