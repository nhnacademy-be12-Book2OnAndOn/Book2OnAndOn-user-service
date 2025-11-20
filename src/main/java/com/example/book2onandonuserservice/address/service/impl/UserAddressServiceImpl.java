package com.example.book2onandonuserservice.address.service.impl;

import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressCreateRequestDto;
import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressUpdateRequestDto;
import com.example.book2onandonuserservice.address.domain.dto.response.UserAddressResponseDto;
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
    private UserAddressResponseDto convertToResponse(Address address) {
        return UserAddressResponseDto.builder()
                .addressId(address.getAddressId())
                .userAddressName(address.getUserAddressName())
                .userAddress(address.getUserAddress())
                .userAddressDetail(address.getUserAddressDetail())
                .isDefault(address.isDefault())
                .build();
    }

    @Override
    public List<UserAddressResponseDto> findByUserId(Long userId) {
        Users user = findUserOrThrow(userId);

        return userAddressRepository.findAllByUser(user).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserAddressResponseDto findByUserIdAndAddressId(Long userId, Long addressId) {
        Users user = findUserOrThrow(userId);

        Address address = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(AddressNotFoundException::new);

        return convertToResponse(address);
    }

    @Override
    @Transactional
    public UserAddressResponseDto save(Long userId, UserAddressCreateRequestDto request) {
        Users user = findUserOrThrow(userId);

        if (userAddressRepository.countByUser(user) >= 10) {
            throw new AddressLimitExceededException();
        }
        if (userAddressRepository.existsByUserAndUserAddressName(user, request.getUserAddressName())) {
            throw new AddressNameDuplicateException();
        }
        //저장할 주소가 대표주소일때 기존 대표주소 -> 일반주소로
        if (request.getIsDefault()) {
            userAddressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(oldDefault -> oldDefault.changeDefaultAddress(false));
        }
        //첫 주소 생성시 대표주소로 설정
        boolean isFirstAddress = userAddressRepository.countByUser(user) == 0;
        boolean shouldBeDefault = request.getIsDefault() || isFirstAddress;

        Address address = Address.builder()
                .user(user)
                .userAddressName(request.getUserAddressName())
                .userAddress(request.getUserAddress())
                .userAddressDetail(request.getUserAddressDetail())
                .isDefault(shouldBeDefault)
                .build();

        Address savedAddress = userAddressRepository.save(address);

        return convertToResponse(savedAddress);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        Users user = findUserOrThrow(userId);

        Address newDefault = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(AddressNotFoundException::new);

        if (newDefault.isDefault()) {
            return;
        }

        userAddressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(oldDefault -> oldDefault.changeDefaultAddress(false));

        newDefault.changeDefaultAddress(true);
    }

    @Override
    @Transactional
    public UserAddressResponseDto update(Long userId, Long addressId, UserAddressUpdateRequestDto request) {
        Users user = findUserOrThrow(userId);

        //엔티티 조회 (소유권 확인까지 동시 수행)
        Address address = userAddressRepository.findByUserAndAddressId(user, addressId)
                .orElseThrow(AddressNotFoundException::new);

        //주소이름 중복 검증
        if (!address.getUserAddressName().equals(request.getUserAddressName()) &&
                userAddressRepository.existsByUserAndUserAddressName(user, request.getUserAddressName())) {
            throw new AddressNameDuplicateException();
        }

        //터티체킹
        address.updateAddressInfo(
                request.getUserAddressName(),
                request.getUserAddress(),
                request.getUserAddressDetail()
        );

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
