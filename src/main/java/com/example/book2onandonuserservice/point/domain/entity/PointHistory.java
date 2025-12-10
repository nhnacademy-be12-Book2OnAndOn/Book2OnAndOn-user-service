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
@Table(name = "point_history")
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

    @NotNull
    @Column(name = "point_expired_date")
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

    @Column(name = "return_id")
    private Long returnId;

    @Column(name = "review_id")
    private Long reviewId;

}
