package com.example.book2onandonuserservice.address.service;

import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressCreateRequestDto;
import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressUpdateRequestDto;
import com.example.book2onandonuserservice.address.domain.dto.response.UserAddressResponseDto;
import java.util.List;

public interface UserAddressService {
    List<UserAddressResponseDto> findByUserId(Long userId);

    UserAddressResponseDto findByUserIdAndAddressId(Long userId, Long addressId);

    UserAddressResponseDto save(Long userId, UserAddressCreateRequestDto request);

    UserAddressResponseDto update(Long userId, Long addressId, UserAddressUpdateRequestDto request);

    void deleteByUserAddressId(Long userId, Long addressId);

    //대표 주소 설정 (추가)
    void setDefaultAddress(Long userId, Long addressId);
}
