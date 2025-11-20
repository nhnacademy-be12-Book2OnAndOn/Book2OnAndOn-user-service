package com.example.book2onandonuserservice.address.domain.dto.request;

import jakarta.validation.constraints.NotNull;
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
