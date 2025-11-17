package com.example.book2onandonuserservice.point.domain.entity;

import com.example.book2onandonuserservice.user.domain.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private Long pointHistoryId;

    @Column(name = "point_history_change")
    @NotNull
    private int pointHistoryChange;

    @Column(name = "total_points")
    @NotNull
    private int totalPoints;

    @Column(name = "point_history_reason", length = 50)
    @Size(max = 50)
    @NotNull
    private String pointHistoryReason;

    @Column(name = "point_created_date")
    @NotNull
    private LocalDateTime pointCreatedDate;

    @Column(name = "point_expired_date")
    @NotNull
    private LocalDateTime pointExpiredDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "return_id")
    private Long returnEntity;

    @Column(name = "order_item_id")
    private Long orderItemId;

    @Column(name = "review_id")
    private Long reviewId;

}
