package com.example.book2onandonuserservice.address.repository;

import com.example.book2onandonuserservice.address.domain.entity.Address;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByUserAndAddressId(Users user, Long addressId);

    long countByUser(Users user);

    List<Address> findAllByUser(Users user);

    boolean existsByUserAndUserAddressName(Users user, String userAddressName);

    //(추가)유저의 현재 대표주소 찾기
    Optional<Address> findByUserAndIsDefaultTrue(Users user);
}
