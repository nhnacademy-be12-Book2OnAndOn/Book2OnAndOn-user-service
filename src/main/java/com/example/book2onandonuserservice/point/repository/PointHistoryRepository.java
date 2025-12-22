package com.example.book2onandonuserservice.point.repository;

import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    /**
     * 회원의 포인트 전체 이력 (마이페이지)
     */
    Page<PointHistory> findAllByUserUserIdOrderByPointCreatedDateDesc(Long userId, Pageable pageable);

    /**
     * 회원의 포인트 적립/사용(필터) 이력 (마이페이지)
     */
    @Query("SELECT p FROM PointHistory p "
            + "WHERE p.user.userId = :userId "
            + "AND p.pointHistoryChange > 0 "
            + "ORDER BY p.pointCreatedDate DESC")
    Page<PointHistory> findEarnedPoints(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM PointHistory p "
            + "WHERE p.user.userId = :userId "
            + "AND p.pointHistoryChange < 0 "
            + "ORDER BY p.pointCreatedDate DESC")
    Page<PointHistory> findUsedPoints(@Param("userId") Long userId, Pageable pageable);

    /**
     * 최신 total 조회 (락 없음 - read 전용)
     */
    Optional<PointHistory> findTop1ByUserUserIdOrderByPointCreatedDateDesc(Long userId);

    /**
     * 최신 total 조회 (락 있음 - write 경합 방지용)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PointHistory p "
            + "WHERE p.user.userId = :userId "
            + "ORDER BY p.pointCreatedDate DESC, p.pointHistoryId DESC")
    Page<PointHistory> findLatestForUpdate(@Param("userId") Long userId, Pageable pageable);

    default Optional<PointHistory> findLatestForUpdateOne(Long userId) {
        Page<PointHistory> page = findLatestForUpdate(userId, PageRequest.of(0, 1));
        return page.isEmpty() ? Optional.empty() : Optional.of(page.getContent().get(0));
    }

    /**
     * 특정 리뷰의 포인트 적립 이력 조회
     */
    List<PointHistory> findByReviewId(Long reviewId);

    /**
     * 특정 주문의 포인트 적립 이력 조회
     */
    List<PointHistory> findByOrderId(Long orderId);

    /**
     * 특정 반품의 포인트 반환 이력 조회
     */
    List<PointHistory> findByRefundId(Long refundId);

    /**
     * 해당 주문에서 USE가 이미 한 번 발생했는지 여부(결제)
     */
    boolean existsByOrderIdAndPointReason(Long orderId, PointReason pointReason);

    /**
     * 회원가입으로 포인트를 받은 적이 있는지 여부(회원가입 유무 판별)
     */
    boolean existsByUserUserIdAndPointReason(Long userId, PointReason pointReason);

    /**
     * 취소(결제취소) 중복 방지용: "orderId + REFUND + refundId null"
     */
    boolean existsByOrderIdAndPointReasonAndRefundIdIsNull(Long orderId, PointReason pointReason);

    /**
     * 실제로 사용된(차감된) 총 포인트 금액을 계산
     */
    @Query("SELECT COALESCE(-SUM(p.pointHistoryChange), 0) " +
            "FROM PointHistory p " +
            "WHERE p.orderId = :orderId " +
            "AND p.pointReason = :reason " +
            "AND p.pointHistoryChange < 0")
    int sumUsedPointByOrder(Long orderId, PointReason reason);

    /**
     * 실제로 사용된(차감된) 총 포인트 금액을 계산 -> 이 주문(orderId)에서 고객이 총 몇 포인트를 사용했는지"를 정확히 파악하여, 그 금액만큼 고객에게 포인트를 복구(롤백)
     */
    List<PointHistory> findByOrderIdAndPointReason(Long orderId, PointReason pointReason);

    /**
     * 이미 만료된 포인트 조회 (배치 처리용) (remainingPoint > 0)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PointHistory p "
            + "WHERE p.user.userId = :userId "
            + "AND p.pointExpiredDate IS NOT NULL "
            + "AND p.pointExpiredDate < :now "
            + "AND p.remainingPoint > 0")
    List<PointHistory> findAlreadyExpiredPoints(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * FIFO(선입선출) 사용을 위한 적립 row 조회 -> 아직 남아 있는 적립 포인트를, 오래된 순서대로 가져오기
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000") // 2초
    })
    @Query("""
            SELECT p
            FROM PointHistory p
            WHERE p.user.userId = :userId
              AND p.pointHistoryChange > 0
              AND p.remainingPoint > 0
              AND (p.pointExpiredDate IS NULL OR p.pointExpiredDate >= :now)
            ORDER BY
              CASE WHEN p.pointExpiredDate IS NULL THEN 1 ELSE 0 END ASC,
              p.pointExpiredDate ASC,
              p.pointCreatedDate ASC,
              p.pointHistoryId ASC
            """)
    List<PointHistory> findPointsForUsage(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * 만료 대상 포인트가 남아 있는 유저 목록
     */
    @Query("SELECT DISTINCT ph.user.userId " +
            "FROM PointHistory ph " +
            "WHERE ph.pointExpiredDate IS NOT NULL " +
            "AND ph.pointExpiredDate <= :now " +
            "AND ph.remainingPoint > 0")
    List<Long> findUserIdsWithExpiredPoints(LocalDateTime now);

    /**
     * 7일 내 소멸 예정 포인트 조회
     */
    @Query("SELECT p FROM PointHistory p " +
            "WHERE p.user.userId = :userId " +
            "AND p.pointExpiredDate IS NOT NULL " +
            "AND p.pointExpiredDate BETWEEN :from AND :to " +
            "AND p.remainingPoint > 0")
    List<PointHistory> findSoonExpiringPoints(@Param("userId") Long userId,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);

    /**
     * 회원탈퇴 시 포인트 0으로 강제 처리
     */
    @Query("SELECT p FROM PointHistory p "
            + "WHERE p.user.userId = :userId "
            + "AND p.pointHistoryChange > 0 "
            + "AND p.remainingPoint > 0")
    List<PointHistory> findAllRemianingPoints(@Param("userId") Long userId);

    /**
     * 이번 달 적립 합계(view)
     */
    @Query("""
            SELECT COALESCE(SUM(ph.pointHistoryChange), 0)
            FROM PointHistory ph
            WHERE ph.user.userId = :userId
              AND ph.pointHistoryChange > 0
              AND ph.pointCreatedDate BETWEEN :from AND :to
            """)
    int sumEarnedInPeriod(Long userId, LocalDateTime from, LocalDateTime to);

    /**
     * 이번 달 사용 합계(view)
     */
    @Query("""
            SELECT COALESCE(SUM(-ph.pointHistoryChange), 0)
            FROM PointHistory ph
            WHERE ph.user.userId = :userId
              AND ph.pointReason = com.example.book2onandonuserservice.point.domain.entity.PointReason.USE
              AND ph.pointCreatedDate BETWEEN :from AND :to
            """)
    int sumUsedInPeriod(Long userId, LocalDateTime from, LocalDateTime to);

    boolean existsByOrderIdAndRefundIdAndPointReason(Long orderId, Long refundId, PointReason pointReason);

}

