package com.example.book2onandonuserservice.user.repository;

import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGradeRepository extends JpaRepository<UserGrade, Long> {
    Optional<UserGrade> findByGradeName(GradeName gradeName);

    boolean existsByGradeName(GradeName gradeName);

    //등급 기준금액이 높은 순서대로 조회
    List<UserGrade> findAllByOrderByGradeCutlineDesc();
}
