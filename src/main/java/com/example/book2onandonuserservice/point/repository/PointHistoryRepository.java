package com.example.book2onandonuserservice.point.repository;

import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // 회원의 포인트 전체 이력 (마이페이지)
    Page<PointHistory> findAllByUserUserIdOrderByPointCreatedDateDesc(Long userId, Pageable pageable);

    @Query("SELECT p FROM PointHistory p "
            + "WHERE p.user.userId = :userId "
            + "AND p.pointHistoryChange > 0 "
            + "ORDER BY p.pointCreatedDate DESC")
    Page<PointHistory> findEarnedPoints(@Param("userId") Long userId, Pageable pageable);
//    Page<PointHistory> findByUserUserIdAndPointHistoryChangeGreaterThanOrderByPointCreatedDateDesc(Long userId, int zero, Pageable pageable)

    @Query("SELECT p FROM PointHistory p "
            + "WHERE p.user.userId = :userId "
            + "AND p.pointHistoryChange < 0 "
            + "ORDER BY p.pointCreatedDate DESC")
    Page<PointHistory> findUsedPoints(@Param("userId") Long userId, Pageable pageable);
//    Page<PointHistory> findByUserUserIdAndPointHistoryChangeLessThanOrderByPointCreatedDateDesc(Long userId, int zero, Pageable pageable);

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

    // 이미 만료된 포인트 조회 (배치 처리용) (remainingPoint > 0)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PointHistory p "
            + "WHERE p.user.userId = :userId "
            + "AND p.pointExpiredDate < :now "
            + "AND p.remainingPoint > 0")
    List<PointHistory> findAlreadyExpiredPoints(@Param("userId") Long userId, @Param("now") LocalDateTime now);
//    List<PointHistory> findByUserUserIdAndPointExpiredDateBeforeAndRemainingPointGreaterThan(Long userId, LocalDateTime now, int minRemainingPoint);

    // FIFO(선입선출) 사용을 위한 적립 row 조회
    // -> 아직 남아 있는 적립 포인트를, 오래된 순서대로 가져오기
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(value = {
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000") // 2초
    })
    @Query("SELECT p FROM PointHistory p " +
            "WHERE p.user.userId = :userId " +
            "AND p.pointHistoryChange > 0 " +
            "AND p.remainingPoint > 0 " +
            "ORDER BY p.pointExpiredDate ASC")
    List<PointHistory> findPointsForUsage(@Param("userId") Long userId);
//    List<PointHistory> findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThanOrderByPointExpiredDateAsc(Long userId, int minChange, int minRemainingPoint);

    // 만료 대상 포인트가 남아 있는 유저 목록
    @Query("select distinct ph.user.userId " +
            "from PointHistory ph " +
            "where ph.pointExpiredDate <= :now " +
            "and ph.remainingPoint > 0")
    List<Long> findUserIdsWithExpiredPoints(LocalDateTime now);

    // 7일 내 소멸 예정 포인트 조회
    @Query("SELECT p FROM PointHistory p " +
            "WHERE p.user.userId = :userId " +
            "AND p.pointExpiredDate BETWEEN :from AND :to " +
            "AND p.remainingPoint > 0")
    List<PointHistory> findSoonExpiringPoints(@Param("userId") Long userId,
                                              @Param("from") LocalDateTime from,
                                              @Param("to") LocalDateTime to);
//    List<PointHistory> findByUserUserIdAndPointExpiredDateBetweenAndRemainingPointGreaterThan(Long userId, LocalDateTime from, LocalDateTime to, int minRemainingPoint);

    // 회원탈퇴 시 포인트 0으로 강제 처리
    @Query("SELECT p FROM PointHistory p "
            + "WHERE p.user.userId = :userId "
            + "AND p.pointHistoryChange > 0 "
            + "AND p.remainingPoint > 0")
    List<PointHistory> findAllRemianingPoints(@Param("userId") Long userId);
//    List<PointHistory> findByUserUserIdAndPointHistoryChangeGreaterThanAndRemainingPointGreaterThan(Long userId, int minChange, int minRemainingPoint);

    // 이번 달 적립 합계
    @Query("""
            SELECT COALESCE(SUM(ph.pointHistoryChange), 0)
            FROM PointHistory ph
            WHERE ph.user.userId = :userId
              AND ph.pointHistoryChange > 0
              AND ph.pointCreatedDate BETWEEN :from AND :to
            """)
    int sumEarnedInPeriod(Long userId, LocalDateTime from, LocalDateTime to);

    // 이번 달 사용 합계
    @Query("""
            SELECT COALESCE(SUM(-ph.pointHistoryChange), 0)
            FROM PointHistory ph
            WHERE ph.user.userId = :userId
              AND ph.pointReason = com.example.book2onandonuserservice.point.domain.entity.PointReason.USE
              AND ph.pointCreatedDate BETWEEN :from AND :to
            """)
    int sumUsedInPeriod(Long userId, LocalDateTime from, LocalDateTime to);
}

