package com.example.book2onandonuserservice.point.repository;

import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {

    // 정책 이름으로 조회 (기본 적립률, 회원가입, 리뷰 등)
    Optional<PointPolicy> findByPolicyName(String policyName);

}
