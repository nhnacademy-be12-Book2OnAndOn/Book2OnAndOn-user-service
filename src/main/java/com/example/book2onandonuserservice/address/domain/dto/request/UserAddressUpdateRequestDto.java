package com.example.book2onandonuserservice.address.domain.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserAddressUpdateRequestDto {
    private Long addressId;

    @Size(max = 20, message = "주소 이름은 20자 이내여야 합니다.")
    private String userAddressName;
    @Size(max = 50, message = "수령인은 50자 이내여야 합니다.")
    private String recipient;

    @Size(max = 11, message = "전화번호는 11자 이내로 작성해주세요.")
    @Pattern(regexp = "^\\d{11}$", message = "전화번호는 '-' 없이 11자리 숫자여야 합니다.") // [수정] 정규식 패턴 추가
    private String phone;

    @Size(max = 10, message = "우편번호는 10자 이내여야 합니다.")
    private String zipCode;


    @Size(max = 255, message = "주소는 255자 이내로 작성해주세요")
    private String userAddress;

    @Size(max = 255, message = "상세주소는 255자 이내로 작성해주세요")
    private String userAddressDetail;

    private Boolean isDefault;

    public boolean getIsDefault() {
        return isDefault != null && isDefault;
    }
}
