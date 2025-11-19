package com.example.book2onandonuserservice.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "UserGrade")
public class UserGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_grade_id")
    private Long gradeId;

    @NotNull
    @Column(name = "user_grade_name", unique = true, length = 20)
    private GradeName gradeName;

    @NotNull
    @Column(name = "user_point_add_rate")
    private Double userPointAddRate;

    @NotNull
    @Column(name = "grade_cutline")
    private Integer gradeCutline;

    //생성자
    public UserGrade(GradeName gradeName, Double userPointAddRate, Integer gradeCutline) {
        this.gradeName = gradeName;
        this.userPointAddRate = userPointAddRate;
        this.gradeCutline = gradeCutline;
    }

    //비즈니스 로직 더디체킹
    public void updateGradeInfo(Double userPointAddRate, Integer gradeCutline) {
        this.userPointAddRate = userPointAddRate;
        this.gradeCutline = gradeCutline;
    }
}
