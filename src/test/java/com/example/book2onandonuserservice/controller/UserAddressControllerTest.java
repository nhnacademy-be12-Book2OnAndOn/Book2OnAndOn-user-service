package com.example.book2onandonuserservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.book2onandonuserservice.address.controller.UserAddressController;
import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressCreateRequestDto;
import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressUpdateRequestDto;
import com.example.book2onandonuserservice.address.domain.dto.response.UserAddressResponseDto;
import com.example.book2onandonuserservice.address.service.UserAddressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserAddressController.class)
@WithMockUser
class UserAddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAddressService userAddressService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/users/me/addresses";
    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final Long TEST_USER_ID = 1L;

    private UserAddressResponseDto createResponseDto(Long id, String alias) {
        return UserAddressResponseDto.builder()
                .addressId(id)
                .userAddressName(alias)
                .recipient("홍길동")
                .phone("010-1234-5678")
                .zipCode("12345")
                .userAddress("서울시 강남구")
                .userAddressDetail("101호")
                .isDefault(false)
                .build();
    }

    @Test
    @DisplayName("회원 주소 목록 조회 (200 OK)")
    void getMyAddresses_Success() throws Exception {
        List<UserAddressResponseDto> responseList = List.of(
                createResponseDto(10L, "집"),
                createResponseDto(11L, "회사")
        );

        given(userAddressService.findByUserId(TEST_USER_ID)).willReturn(responseList);

        mockMvc.perform(get(BASE_URL)
                        .header(USER_ID_HEADER, TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    @DisplayName("특정 주소 상세 조회 (200 OK)")
    void getAddressDetails_Success() throws Exception {
        Long addressId = 10L;
        UserAddressResponseDto response = createResponseDto(addressId, "집");

        given(userAddressService.findByUserIdAndAddressId(TEST_USER_ID, addressId))
                .willReturn(response);

        mockMvc.perform(get(BASE_URL + "/{addressId}", addressId)
                        .header(USER_ID_HEADER, TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressId").value(addressId));
    }

    @Test
    @DisplayName("주소 추가 (201 Created)")
    void createAddress_Success() throws Exception {
        UserAddressCreateRequestDto request = new UserAddressCreateRequestDto();
        ReflectionTestUtils.setField(request, "userAddressName", "집");
        ReflectionTestUtils.setField(request, "recipient", "홍길동");
        ReflectionTestUtils.setField(request, "phone", "01012345678");
        ReflectionTestUtils.setField(request, "zipCode", "12345");
        ReflectionTestUtils.setField(request, "userAddress", "서울시");
        ReflectionTestUtils.setField(request, "userAddressDetail", "상세");
        ReflectionTestUtils.setField(request, "isDefault", true);

        UserAddressResponseDto response = createResponseDto(100L, "집");

        given(userAddressService.save(eq(TEST_USER_ID), any(UserAddressCreateRequestDto.class)))
                .willReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .header(USER_ID_HEADER, TEST_USER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addressId").value(100L));
    }

    @Test
    @DisplayName("주소 수정 (200 OK)")
    void updateAddress_Success() throws Exception {
        Long addressId = 100L;
        UserAddressUpdateRequestDto request = new UserAddressUpdateRequestDto();
        ReflectionTestUtils.setField(request, "userAddressName", "회사");
        ReflectionTestUtils.setField(request, "recipient", "김철수");
        ReflectionTestUtils.setField(request, "phone", "01098765432");
        ReflectionTestUtils.setField(request, "zipCode", "54321");
        ReflectionTestUtils.setField(request, "userAddress", "부산시");
        ReflectionTestUtils.setField(request, "userAddressDetail", "202호");
        ReflectionTestUtils.setField(request, "isDefault", false);

        UserAddressResponseDto response = createResponseDto(addressId, "회사");

        given(userAddressService.update(eq(TEST_USER_ID), eq(addressId), any(UserAddressUpdateRequestDto.class)))
                .willReturn(response);

        mockMvc.perform(put(BASE_URL + "/{addressId}", addressId)
                        .header(USER_ID_HEADER, TEST_USER_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userAddressName").value("회사"));
    }

    @Test
    @DisplayName("주소 삭제 (204 No Content)")
    void deleteAddress_Success() throws Exception {
        Long addressId = 100L;
        doNothing().when(userAddressService).deleteByUserAddressId(TEST_USER_ID, addressId);

        mockMvc.perform(delete(BASE_URL + "/{addressId}", addressId)
                        .header(USER_ID_HEADER, TEST_USER_ID)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userAddressService).deleteByUserAddressId(TEST_USER_ID, addressId);
    }
}