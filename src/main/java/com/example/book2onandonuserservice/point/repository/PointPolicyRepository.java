package com.example.book2onandonuserservice.point.repository;

import com.example.book2onandonuserservice.point.domain.entity.PointPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointPolicyRepository extends JpaRepository<PointPolicy, Long> {

    // 정책 이름 조회
    Optional<PointPolicy> findByPolicyName(String policyName);

}
