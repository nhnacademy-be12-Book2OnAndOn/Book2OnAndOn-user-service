package com.example.book2onandonuserservice.address.service.impl;

import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressCreateRequest;
import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressUpdateRequest;
import com.example.book2onandonuserservice.address.domain.dto.response.UserAddressResponse;
import com.example.book2onandonuserservice.address.domain.entity.Address;
import com.example.book2onandonuserservice.address.exception.AddressLimitExceededException;
import com.example.book2onandonuserservice.address.exception.AddressNameDuplicateException;
import com.example.book2onandonuserservice.address.exception.AddressNotFoundException;
import com.example.book2onandonuserservice.address.repository.UserAddressRepository;
import com.example.book2onandonuserservice.address.service.UserAddressService;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAddressServiceImpl implements UserAddressService {
    private final UserAddressRepository userAddressRepository;
    private final UsersRepository usersRepository;

    //ID로 사용자 조회
    private Users findUserOrThrow(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    //Entity를 Response DTO로 변환
    private UserAddressResponse convertToResponse(Address address) {
        return UserAddressResponse.builder()
                .addressId(address.getAddressId())
                .userAddressName(address.getUserAddressName())
                .userAddress(address.getUserAddress())
                .userAddressDetail(address.getUserAddressDetail())
                .build();
    }

    @Override
    public List<UserAddressResponse> findByUserId(Long userId) {
        Users user = findUserOrThrow(userId);

        return userAddressRepository.findAllByUser(user).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserAddressResponse findByUserIdAndAddressId(Long userId, Long addressId) {
        Users user = findUserOrThrow(userId);

        Address address = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(AddressNotFoundException::new);

        return convertToResponse(address);
    }

    @Override
    @Transactional
    public UserAddressResponse save(Long userId, UserAddressCreateRequest request) {
        Users user = findUserOrThrow(userId);

        if (userAddressRepository.countByUser(user) >= 10) {
            throw new AddressLimitExceededException();
        }
        if (userAddressRepository.existsByUserAndUserAddressName(user, request.getUserAddressName())) {
            throw new AddressNameDuplicateException();
        }

        Address address = Address.builder()
                .user(user)
                .userAddressName(request.getUserAddressName())
                .userAddress(request.getUserAddress())
                .userAddressDetail(request.getUserAddressDetail())
                .build();

        Address savedAddress = userAddressRepository.save(address);

        return convertToResponse(savedAddress);
    }

    @Override
    @Transactional
    public UserAddressResponse update(Long userId, Long addressId, UserAddressUpdateRequest request) {
        Users user = findUserOrThrow(userId);

        if (!request.getAddressId().equals(addressId)) {
            throw new RuntimeException("요청 ID가 일치하지 않습니다.");
        }

        Address address = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(AddressNotFoundException::new);

        if (!address.getUserAddressName().equals(request.getUserAddressName()) &&
                userAddressRepository.existsByUserAndUserAddressName(user, request.getUserAddressName())) {
            throw new AddressNameDuplicateException();
        }

        address.setUserAddressName(request.getUserAddressName());
        address.setUserAddress(request.getUserAddress());
        address.setUserAddressDetail(request.getUserAddressDetail());

        return convertToResponse(address);
    }

    @Override
    @Transactional
    public void deleteByUserAddressId(Long userId, Long addressId) {
        Users user = findUserOrThrow(userId);

        Address address = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(AddressNotFoundException::new);

        userAddressRepository.delete(address);
    }
}
