package com.example.book2onandonuserservice.user.repository;

import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    //ID 중복 체크
    boolean existsByUserLoginId(String userLoginId);

    //ID로 사용자 찾기
    Optional<Users> findByUserLoginId(String userLoginId);

    //이메일로 사용자 찾기
    Optional<Users> findByEmail(String email);

    //닉네임으로 사용자 찾기
    Optional<Users> findByNickname(String nickname);
    
    //닉네임 중복 체크
    boolean existsByNickname(String nickname);

}
