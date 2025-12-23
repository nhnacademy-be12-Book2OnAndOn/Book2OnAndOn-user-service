package com.example.book2onandonuserservice.point.domain.entity;

import com.example.book2onandonuserservice.user.domain.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "point_history",
        indexes = {
                @Index(name = "idx_point_user_created", columnList = "user_id, point_created_date"),
                @Index(name = "idx_point_user_expired", columnList = "user_id, point_expired_date"),
                @Index(name = "idx_point_order_reason", columnList = "order_id, point_reason"),
                @Index(name = "idx_point_return", columnList = "return_id"),
                @Index(name = "idx_point_review", columnList = "review_id")
        }
)
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private Long pointHistoryId;

    @NotNull
    @Column(name = "point_history_change")
    private int pointHistoryChange;

    @NotNull
    @Column(name = "total_points")
    private int totalPoints;

    @NotNull
    @Column(name = "point_created_date")
    private LocalDateTime pointCreatedDate;

    @Column(name = "point_expired_date") // 차감 row는 만료일 개념이 없으므로
    private LocalDateTime pointExpiredDate;

    @Column(name = "remaining_point")
    private Integer remainingPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_reason", length = 30, nullable = false)
    private PointReason pointReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "refund_id")
    private Long refundId;

    @Column(name = "review_id")
    private Long reviewId;

}
