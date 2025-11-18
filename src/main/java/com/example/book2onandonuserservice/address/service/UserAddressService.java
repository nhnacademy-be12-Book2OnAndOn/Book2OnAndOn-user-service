package com.example.book2onandonuserservice.address.service;

import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressCreateRequest;
import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressUpdateRequest;
import com.example.book2onandonuserservice.address.domain.dto.response.UserAddressResponse;
import java.util.List;

public interface UserAddressService {
    List<UserAddressResponse> findByUserId(Long userId);

    UserAddressResponse findByUserIdAndAddressId(Long userId, Long addressId);

    UserAddressResponse save(Long userId, UserAddressCreateRequest request);

    UserAddressResponse update(Long userId, Long addressId, UserAddressUpdateRequest request);

    void deleteByUserAddressId(Long userId, Long addressId);
}
