package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressCreateRequestDto;
import com.example.book2onandonuserservice.address.domain.dto.request.UserAddressUpdateRequestDto;
import com.example.book2onandonuserservice.address.domain.dto.response.UserAddressResponseDto;
import com.example.book2onandonuserservice.address.domain.entity.Address;
import com.example.book2onandonuserservice.address.exception.AddressLimitExceededException;
import com.example.book2onandonuserservice.address.exception.AddressNameDuplicateException;
import com.example.book2onandonuserservice.address.exception.AddressNotFoundException;
import com.example.book2onandonuserservice.address.repository.UserAddressRepository;
import com.example.book2onandonuserservice.address.service.impl.UserAddressServiceImpl;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceImplTest {

    @InjectMocks
    private UserAddressServiceImpl userAddressService;

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UsersRepository usersRepository;

    private Users dummyUser;

    @BeforeEach
    void setUp() {
        dummyUser = new Users();
        ReflectionTestUtils.setField(dummyUser, "userId", 1L);
    }

    private UserAddressCreateRequestDto createRequest(String alias, boolean isDefault) {
        UserAddressCreateRequestDto request = new UserAddressCreateRequestDto();
        ReflectionTestUtils.setField(request, "userAddressName", alias);
        ReflectionTestUtils.setField(request, "userAddress", "광주광역시 동구");
        ReflectionTestUtils.setField(request, "userAddressDetail", "101호");
        ReflectionTestUtils.setField(request, "isDefault", isDefault);
        return request;
    }

    // 1. 조회 관련 테스트
    @Test
    @DisplayName("사용자 ID로 주소 목록 조회 성공")
    void findByUserId_Success() {
        Address addr1 = Address.builder().addressId(10L).user(dummyUser).userAddressName("집").build();
        Address addr2 = Address.builder().addressId(11L).user(dummyUser).userAddressName("회사").build();

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.findAllByUser(dummyUser)).willReturn(List.of(addr1, addr2));

        List<UserAddressResponseDto> result = userAddressService.findByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAddressId()).isEqualTo(10L);
        assertThat(result.get(1).getAddressId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("특정 주소 상세 조회 성공")
    void findByUserIdAndAddressId_Success() {
        Long addressId = 100L;
        Address address = Address.builder().addressId(addressId).user(dummyUser).userAddressName("집").build();

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.findByUserAndAddressId(dummyUser, addressId)).willReturn(Optional.of(address));

        UserAddressResponseDto result = userAddressService.findByUserIdAndAddressId(1L, addressId);

        assertThat(result.getAddressId()).isEqualTo(addressId);
        assertThat(result.getUserAddressName()).isEqualTo("집");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 예외 발생")
    void findUser_Fail_UserNotFound() {
        given(usersRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userAddressService.findByUserId(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // 2. 저장 관련 테스트 (save)
    @Test
    @DisplayName("주소 추가 성공 - 첫 번째 주소는 강제 기본 배송지")
    void save_FirstAddress_IsDefault() {
        UserAddressCreateRequestDto request = createRequest("집", false);

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.countByUser(dummyUser)).willReturn(0L);
        given(userAddressRepository.existsByUserAndUserAddressName(dummyUser, "집")).willReturn(false);
        given(userAddressRepository.save(any(Address.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserAddressResponseDto response = userAddressService.save(1L, request);

        assertThat(response.isDefault()).isTrue();
    }

    @Test
    @DisplayName("주소 추가 성공 - 새로운 기본 배송지 설정 시 기존 기본 배송지 해제")
    void save_NewDefault_UnsetsOld() {
        UserAddressCreateRequestDto request = createRequest("회사", true);
        Address oldDefault = Address.builder().user(dummyUser).userAddressName("집").isDefault(true).build();

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.countByUser(dummyUser)).willReturn(1L);
        given(userAddressRepository.existsByUserAndUserAddressName(dummyUser, "회사")).willReturn(false);
        given(userAddressRepository.findByUserAndIsDefaultTrue(dummyUser)).willReturn(Optional.of(oldDefault));
        given(userAddressRepository.save(any(Address.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserAddressResponseDto response = userAddressService.save(1L, request);

        assertThat(response.isDefault()).isTrue();
        assertThat(oldDefault.isDefault()).isFalse();
    }

    @Test
    @DisplayName("주소 추가 실패 - 10개 초과")
    void save_Fail_LimitExceeded() {
        UserAddressCreateRequestDto request = createRequest("별장", false);
        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.countByUser(dummyUser)).willReturn(10L);

        assertThatThrownBy(() -> userAddressService.save(1L, request))
                .isInstanceOf(AddressLimitExceededException.class);
    }

    @Test
    @DisplayName("주소 추가 실패 - 주소 이름 중복")
    void save_Fail_DuplicateAlias() {
        UserAddressCreateRequestDto request = createRequest("집", false);
        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.countByUser(dummyUser)).willReturn(1L);
        given(userAddressRepository.existsByUserAndUserAddressName(dummyUser, "집")).willReturn(true);

        assertThatThrownBy(() -> userAddressService.save(1L, request))
                .isInstanceOf(AddressNameDuplicateException.class);
    }

    // 3. 기본 배송지 설정 테스트 (setDefaultAddress)
    @Test
    @DisplayName("기본 배송지 설정 성공 - 기존 기본 배송지 해제 후 새 설정")
    void setDefaultAddress_Success() {
        Long addressId = 100L;
        Address oldDefault = Address.builder().user(dummyUser).userAddressName("집").isDefault(true).build();
        Address newDefault = Address.builder().addressId(addressId).user(dummyUser).userAddressName("회사")
                .isDefault(false).build();

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.findByUserAndAddressId(dummyUser, addressId)).willReturn(Optional.of(newDefault));
        given(userAddressRepository.findByUserAndIsDefaultTrue(dummyUser)).willReturn(Optional.of(oldDefault));

        userAddressService.setDefaultAddress(1L, addressId);

        assertThat(oldDefault.isDefault()).isFalse();
        assertThat(newDefault.isDefault()).isTrue();
    }

    @Test
    @DisplayName("기본 배송지 설정 - 이미 기본 배송지라면 아무 일도 안 함")
    void setDefaultAddress_AlreadyDefault() {
        Long addressId = 100L;
        Address alreadyDefault = Address.builder().addressId(addressId).user(dummyUser).isDefault(true).build();

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.findByUserAndAddressId(dummyUser, addressId)).willReturn(
                Optional.of(alreadyDefault));

        userAddressService.setDefaultAddress(1L, addressId);

        verify(userAddressRepository, never()).findByUserAndIsDefaultTrue(any());
        assertThat(alreadyDefault.isDefault()).isTrue();
    }

    // 4. 수정 관련 테스트 (update)
    @Test
    @DisplayName("주소 수정 성공 (기본 정보 변경)")
    void update_Success_BasicInfo() {
        Long addressId = 100L;
        UserAddressUpdateRequestDto request = new UserAddressUpdateRequestDto();
        ReflectionTestUtils.setField(request, "addressId", addressId);
        ReflectionTestUtils.setField(request, "userAddressName", "주소2");
        ReflectionTestUtils.setField(request, "userAddress", "광주광역시");
        ReflectionTestUtils.setField(request, "isDefault", false);

        Address address = Address.builder()
                .user(dummyUser).userAddressName("주소1").userAddress("서울특별시").build();

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.findByUserAndAddressId(dummyUser, addressId)).willReturn(Optional.of(address));
        given(userAddressRepository.existsByUserAndUserAddressName(dummyUser, "주소2")).willReturn(false);

        UserAddressResponseDto response = userAddressService.update(1L, addressId, request);

        assertThat(address.getUserAddressName()).isEqualTo("주소2");
        assertThat(address.getUserAddress()).isEqualTo("광주광역시");
    }

    @Test
    @DisplayName("주소 수정 성공 - 기본 배송지로 변경 시 기존 해제")
    void update_Success_ChangeToDefault() {
        Long addressId = 100L;
        UserAddressUpdateRequestDto request = new UserAddressUpdateRequestDto();
        ReflectionTestUtils.setField(request, "userAddressName", "주소1");
        ReflectionTestUtils.setField(request, "isDefault", true);

        Address targetAddress = Address.builder().user(dummyUser).userAddressName("주소1").isDefault(false).build();
        Address oldDefault = Address.builder().user(dummyUser).userAddressName("옛날기본").isDefault(true).build();

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.findByUserAndAddressId(dummyUser, addressId)).willReturn(
                Optional.of(targetAddress));
        given(userAddressRepository.findByUserAndIsDefaultTrue(dummyUser)).willReturn(Optional.of(oldDefault));

        userAddressService.update(1L, addressId, request);

        assertThat(targetAddress.isDefault()).isTrue();
        assertThat(oldDefault.isDefault()).isFalse();
    }

    @Test
    @DisplayName("주소 수정 실패 - 변경하려는 이름이 이미 존재함")
    void update_Fail_DuplicateAlias() {
        Long addressId = 100L;
        UserAddressUpdateRequestDto request = new UserAddressUpdateRequestDto();
        ReflectionTestUtils.setField(request, "userAddressName", "중복된이름");

        Address address = Address.builder().user(dummyUser).userAddressName("내주소").build();

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.findByUserAndAddressId(dummyUser, addressId)).willReturn(Optional.of(address));
        given(userAddressRepository.existsByUserAndUserAddressName(dummyUser, "중복된이름")).willReturn(true);

        assertThatThrownBy(() -> userAddressService.update(1L, addressId, request))
                .isInstanceOf(AddressNameDuplicateException.class);
    }

    // 5. 삭제 관련 테스트 (delete)
    @Test
    @DisplayName("주소 삭제 성공")
    void delete_Success() {
        Long addressId = 100L;
        Address address = Address.builder().user(dummyUser).build();

        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.findByUserAndAddressId(dummyUser, addressId)).willReturn(Optional.of(address));

        userAddressService.deleteByUserAddressId(1L, addressId);

        verify(userAddressRepository, times(1)).delete(address);
    }

    @Test
    @DisplayName("주소 삭제 실패 - 내 주소가 아님")
    void delete_Fail_NotMyAddress() {
        Long addressId = 100L;
        given(usersRepository.findById(1L)).willReturn(Optional.of(dummyUser));
        given(userAddressRepository.findByUserAndAddressId(dummyUser, addressId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userAddressService.deleteByUserAddressId(1L, addressId))
                .isInstanceOf(AddressNotFoundException.class);
    }
}