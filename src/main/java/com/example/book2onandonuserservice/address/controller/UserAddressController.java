package com.example.book2onandonuserservice.address.controller;

import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressCreateRequest;
import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressUpdateRequest;
import com.example.book2onandonuserservice.address.domain.dto.response.UserAddressResponse;
import com.example.book2onandonuserservice.address.service.UserAddressService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/addresses")
@RequiredArgsConstructor
public class UserAddressController {
    private final UserAddressService userAddressService;
    private static final String USER_ID_HEADER = "X-USER-ID";

    //GET: 회원 주소 목록 조회 (200 OK)
    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> getMyAddresses(
            @RequestHeader(USER_ID_HEADER) Long userId
    ) {
        List<UserAddressResponse> addresses = userAddressService.findByUserId(userId);
        return ResponseEntity.ok(addresses);
    }

    //GET /{addressId} : 특정 주소 상세 조회 (200 OK)
    @GetMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> getAddressDetails(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long addressId
    ) {
        UserAddressResponse address = userAddressService.findByUserIdAndAddressId(userId, addressId);
        return ResponseEntity.ok(address);
    }

    //POST: 클라이언트 주소 추가 (201 Created)
    @PostMapping
    public ResponseEntity<UserAddressResponse> createAddress(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody UserAddressCreateRequest request
    ) {
        UserAddressResponse responseDto = userAddressService.save(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    //PUT /{addressId}: 특정 주소 수정 (200 OK)
    @PutMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> updateAddress(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody UserAddressUpdateRequest request
    ) {
        UserAddressResponse responseDto = userAddressService.update(userId, addressId, request);
        return ResponseEntity.ok(responseDto);
    }

    //DELETE /{addressId}: 특정 주소 삭제 (204 No Content)
    @DeleteMapping("/{addressId}")
    public ResponseEntity<UserAddressResponse> deleteAddress(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long addressId
    ) {
        userAddressService.deleteByUserAddressId(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}
