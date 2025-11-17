package com.example.book2onandonuserservice.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "UserGrade")
public class UserGrade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_grade_id")
    private Long gradeId;

    @NotNull
    @Column(name = "user_grade_name", unique = true, length = 10)
    @Size(max = 10)
    private String grade;

    @NotNull
    @Column(name = "user_point_add_rate")
    private Double userPointAddRate;

    @NotNull
    @Column(name = "grade_cutline")
    private Integer gradeCutline;
}
