package com.example.book2onandonuserservice.point.repository;

import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 회원의 포인트 전체 이력 (마이페이지)
    Page<PointHistory> findAllByUserUserIdOrderByPointCreatedDateDesc(Long userId, Pageable pageable);

    // 회원의 현재 보유 포인트 (가장 최근 이력 1건)
    Optional<PointHistory> findTop1ByUserUserIdOrderByPointCreatedDateDesc(Long userId);

    // 특정 리뷰의 포인트 적립 이력 조회
    List<PointHistory> findByReviewId(Long reviewId);

    // 특정 주문의 포인트 적립 이력 조회
    List<PointHistory> findByOrderId(Long orderId);

    // 특정 반품의 포인트 반환 이력 조회
    List<PointHistory> findByReturnId(Long returnId);

    // 해당 주문에서 USE가 이미 한 번 발생했는지 여부(결제)
    boolean existsByOrderIdAndPointReason(Long orderId, PointReason pointReason);

    // 회원가입으로 포인트를 받은 적이 있는지 여부(회원가입 유무 판별)
    boolean existsByUserUserIdAndPointReason(Long userId, PointReason pointReason);

    @Query("SELECT COALESCE(-SUM(p.pointHistoryChange), 0) " +
            "FROM PointHistory p " +
            "WHERE p.orderId = :orderId " +
            "AND p.pointReason = :reason " +
            "AND p.pointHistoryChange < 0")
    int sumUsedPointByOrder(Long orderId, PointReason reason);

    List<PointHistory> findByOrderIdAndPointReason(Long orderId, PointReason pointReason);

    // 만료 대상 조회 (remainingPoint > 0)
    List<PointHistory> findByUserUserIdAndPointExpiredDateBeforeAndRemainingPointGreaterThan(
            Long userId, LocalDateTime now, int minRemainingPoint);

    // FIFO(선입선출) 사용을 위한 적립 row 조회
    // -> 아직 남아 있는 적립 포인트를, 오래된 순서대로 가져오기
    List<PointHistory> findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThanOrderByPointExpiredDateAsc(
            Long userId, int minChange, int minRemainingPoint);

    // 만료 대상 포인트가 남아 있는 유저 목록
    @Query("select distinct ph.user.userId " +
            "from PointHistory ph " +
            "where ph.pointExpiredDate <= :now " +
            "and ph.remainingPoint > 0")
    List<Long> findUserIdsWithExpiredPoints(LocalDateTime now);

    // 7일 내 소멸 예정 포인트 조회
    List<PointHistory> findByUserUserIdAndPointExpiredDateBetweenAndRemainingPointGreaterThan(
            Long userId, LocalDateTime from, LocalDateTime to, int minRemainingPoint);

}

