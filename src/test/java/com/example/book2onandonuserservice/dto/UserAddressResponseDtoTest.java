package com.example.book2onandonuserservice.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.book2onandonuserservice.address.domain.dto.response.UserAddressResponseDto;
import com.example.book2onandonuserservice.address.domain.entity.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserAddressResponseDtoTest {

    @Test
    @DisplayName("Entity -> DTO 변환 테스트 (fromEntity)")
    void fromEntity_Test() {
        Address address = Address.builder()
                .addressId(1L)
                .userAddressName("우리집")
                .recipient("홍길동")
                .phone("010-1234-5678")
                .zipCode("12345")
                .userAddress("서울시 강남구")
                .userAddressDetail("101호")
                .isDefault(true)
                .build();

        UserAddressResponseDto dto = UserAddressResponseDto.fromEntity(address);

        assertThat(dto.getAddressId()).isEqualTo(1L);
        assertThat(dto.getUserAddressName()).isEqualTo("우리집");
        assertThat(dto.getRecipient()).isEqualTo("홍길동");
        assertThat(dto.getPhone()).isEqualTo("010-1234-5678");
        assertThat(dto.getZipCode()).isEqualTo("12345");
        assertThat(dto.getUserAddress()).isEqualTo("서울시 강남구");
        assertThat(dto.getUserAddressDetail()).isEqualTo("101호");
        assertThat(dto.isDefault()).isTrue();
    }

    @Test
    @DisplayName("Lombok 빌더 및 Getter/Setter 테스트")
    void builderAndGetter_Test() {
        UserAddressResponseDto dto = UserAddressResponseDto.builder()
                .addressId(2L)
                .userAddressName("회사")
                .recipient("김철수")
                .phone("010-9876-5432")
                .zipCode("54321")
                .userAddress("부산시")
                .userAddressDetail("202호")
                .isDefault(false)
                .build();

        assertThat(dto.getAddressId()).isEqualTo(2L);
        assertThat(dto.getUserAddressName()).isEqualTo("회사");
        assertThat(dto.getRecipient()).isEqualTo("김철수");
        assertThat(dto.getPhone()).isEqualTo("010-9876-5432");
        assertThat(dto.getZipCode()).isEqualTo("54321");
        assertThat(dto.getUserAddress()).isEqualTo("부산시");
        assertThat(dto.getUserAddressDetail()).isEqualTo("202호");
        assertThat(dto.isDefault()).isFalse();
    }

    @Test
    @DisplayName("기본 생성자 및 AllArgs 생성자 테스트")
    void constructor_Test() {
        UserAddressResponseDto emptyDto = new UserAddressResponseDto();
        assertThat(emptyDto).isNotNull();

        UserAddressResponseDto fullDto = new UserAddressResponseDto(
                3L, "별장", "이영희", "010-1111-2222", "67890",
                "제주시", "101동", false
        );

        assertThat(fullDto.getAddressId()).isEqualTo(3L);
        assertThat(fullDto.getUserAddressName()).isEqualTo("별장");
    }
}