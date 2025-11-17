package com.example.book2onandonuserservice.point.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@NoArgsConstructor
@Builder
@AllArgsConstructor

public class PointPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_policy_id")
    private Long policyId;

    @NotNull
    @Column(name = "point_policy_name", unique = true, length = 50)
    @Size(max = 50)
    private String policyName;

    @Column(name = "point_add_rate")
    private Double addRate;

    @Column(name = "point_add_point")
    private Integer addPoint;
}
