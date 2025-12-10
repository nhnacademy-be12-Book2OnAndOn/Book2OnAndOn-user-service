package com.example.book2onandonuserservice.point.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "point_policy")
public class PointPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_policy_id")
    private Integer policyId;

    @NotNull(message = "정책명는 필수입니다")
    @Size(max = 50, message = "정책명은 최대 50자까지 입력 가능합니다.")
    @Column(name = "point_policy_name", unique = true, length = 50)
    private String policyName;

//    @Column(name = "point_add_rate")
//    private Double policyAddRate;

    @Column(name = "point_add_point")
    private Integer policyAddPoint;

    @NotNull(message = "정책 활성화 여부는 필수입니다")
    @Column(name = "point_is_active", nullable = false)
    private Boolean policyIsActive;

}
