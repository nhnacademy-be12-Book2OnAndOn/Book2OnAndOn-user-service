package com.example.book2onandonuserservice.user.repository;

import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Users> findAllByStatus(Status status);

    //이름 + 이메일로 찾기 (아이디 찾기)
    Optional<Users> findByNameAndEmail(String name, String email);

    //아이디 + 이메일로 찾기 (비밀번호 찾기)
    Optional<Users> findByUserLoginIdAndEmail(String userLoginId, String email);

    @Query("SELECT u.userId FROM Users u WHERE MONTH(u.birth) = :month")
    Slice<Long> findIdsByBirthMonth(@Param("month") int month, Pageable pageable);
}
