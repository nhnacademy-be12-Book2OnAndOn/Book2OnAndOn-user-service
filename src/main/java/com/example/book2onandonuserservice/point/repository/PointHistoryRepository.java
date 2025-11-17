package com.example.book2onandonuserservice.point.repository;

import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 현재 보유 포인트 내역 (가장 최신 이력 1건)
    Optional<PointHistory> findTop1ByUserOrderByPointCreatedDateDesc(Users user);

    // 포인트 전체 이력 조회
    Page<PointHistory> findAllByUser(Users user, Pageable pageable);

    // 특정 주문 항목에 사용된 포인트 조회 (취소/반품용)
    List<PointHistory> findByOrderItemId(Long orderItemId);
}

