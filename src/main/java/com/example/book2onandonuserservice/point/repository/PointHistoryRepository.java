package com.example.book2onandonuserservice.point.repository;

import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 회원의 포인트 전체 이력 (마이페이지)
    Page<PointHistory> findAllByUserUserIdOrderByPointCreatedDateDesc(Long userId, Pageable pageable);

    // 회원의 현재 보유 포인트 (가장 최근 이력 1건)
    Optional<PointHistory> findTop1ByUserUserIdOrderByPointCreatedDateDesc(Long userId);

    // 특정 리뷰의 포인트 적립 이력 조회 (리뷰 삭제 시 취소, 중복 리뷰 적립 방지)
    List<PointHistory> findByReviewId(Long reviewId);

    // 특정 반품의 포인트 이력 조회 (반품 포인트 반환용)
    List<PointHistory> findByReturnId(Long returnId);

    // 특정 주문항목의 포인트 이력 조회 (적립/사용/취소/반품 모두 포함)
    List<PointHistory> findByOrderItemId(Long orderItemId);

    // 해당 주문에서 USE가 이미 한 번 발생했는지 여부
    boolean existsByOrderItemIdAndPointReason(Long orderItemId, PointReason pointReason);

    // 만료 대상 row (remainingPoint > 0)
    // -> 유저의 적립 포인트 중에서, 만료일이 지났는데 아직 쓰이지 않고 남아 있는 포인트 리스트
    List<PointHistory> findByUserUserIdAndPointExpiredDateBeforeAndRemainingPointGreaterThan(Long userId,
                                                                                             LocalDateTime now,
                                                                                             int minRemainingPoint);

    // FIFO 사용을 위한 적립 row 조회
    // -> 유저의 아직 다 안 써서 남아 있는 적립 포인트를, 오래된 순서대로(선입선출) 가져오기
    List<PointHistory> findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThanOrderByPointCreatedDateAsc(
            Long userId, int minChange, int minRemainingPoint);

    boolean existsByUserUserIdAndPointReason(Long userId, PointReason pointReason);

}

