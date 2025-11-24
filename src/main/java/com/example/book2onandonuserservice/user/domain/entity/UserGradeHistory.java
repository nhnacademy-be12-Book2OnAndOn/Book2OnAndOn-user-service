package com.example.book2onandonuserservice.user.domain.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_grade_history")
public class UserGradeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_grade_history_id")
    private Long id;

    @Column(name = "previous_grade_name", length = 20)
    private String previousGradeName;


    @Column(name = "new_grade_name", nullable = false, length = 20)
    private String newGradeName;

    @Column(name = "grade_change_reason", nullable = false, length = 200)
    private String changeReason;

    @Column(name = "grade_changed_at", nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // FK
    private Users user;

    public UserGradeHistory(Users user, String previousGradeName, String newGradeName, String changeReason) {
        this.user = user;
        this.previousGradeName = previousGradeName;
        this.newGradeName = newGradeName;
        this.changeReason = changeReason;
        this.changedAt = LocalDateTime.now();
    }
}
