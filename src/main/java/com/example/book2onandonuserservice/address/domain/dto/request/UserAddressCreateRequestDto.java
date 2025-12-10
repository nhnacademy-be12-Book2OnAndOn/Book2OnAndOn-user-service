package com.example.book2onandonuserservice.address.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class UserAddressCreateRequestDto {
    @Size(max = 20)
    @NotNull(message = "주소 이름은 필수입니다.")
    private String userAddressName;

    @Size(max = 50)
    @NotEmpty(message = "수령인은 필수입니다.")
    private String recipient;

    @NotBlank(message = "연락처는 필수입니다.")
    @Size(max = 11, message = "전화번호는 11자 이내로 작성해주세요.")
    @Pattern(regexp = "^\\d{11}$", message = "전화번호는 '-' 없이 11자리 숫자여야 합니다.") // [수정] 정규식 패턴 추가
    String phone;

    @NotBlank(message = "우편번호는 필수입니다.")
    @Size(max = 10)
    private String zipCode;

    @NotNull
    @Size(min = 1, max = 100, message = "주소는 1자 이상 100자 이내로 작성해주세요")
    @NotNull(message = "주소는 필수입니다.")
    private String userAddress;

    @Size(max = 100, message = "상세주소는 100자 이내로 작성해주세요")
    private String userAddressDetail;

    private Boolean isDefault;

    public boolean getIsDefault() {
        return isDefault != null && isDefault;
    }

}
