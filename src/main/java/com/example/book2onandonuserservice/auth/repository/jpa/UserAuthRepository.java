package com.example.book2onandonuserservice.auth.repository.jpa;

import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    //provider와 providerId로 인증 정보를 찾음
    Optional<UserAuth> findByProviderAndProviderUserId(String provider, String providerUserId);

    //사용자가 이미 해당 provider로 연동했는지 확인
    Optional<UserAuth> findByUserAndProvider(Users user, String provider);

    //특정 사용자의 모든 인증수단 목록을 조회
    List<UserAuth> findAllByUser(Users user);

    //특정 사용자가 해당 authId를 소유하고 있는지 확인
    Optional<UserAuth> findByUserAndAuthId(@NotNull Users user, Long authId);

    //특정 사용자가 가진 총 인증 수단의 개수 확인
    long countByUser(Users user);
}
