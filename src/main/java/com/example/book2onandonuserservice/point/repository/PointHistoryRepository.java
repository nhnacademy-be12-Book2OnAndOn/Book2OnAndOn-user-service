package com.example.book2onandonuserservice.point.repository;

import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 특정 회원의 현재 보유 포인트 (가장 최근 이력 1건)
    Optional<PointHistory> findTop1ByUserUserIdOrderByPointCreatedDateDesc(Long userId);

    // 특정 회원의 포인트 전체 이력 (마이페이지)
    Page<PointHistory> findAllByUserUserId(Long userId, Pageable pageable);

    // 특정 주문 항목에 사용된 포인트/적립 이력 조회 (취소/반품용)
    List<PointHistory> findByOrderItemId(Long orderItemId);

    // 정책 조회용
    Optional<PointHistory> findByPointHistoryReason(String policyName);
}

