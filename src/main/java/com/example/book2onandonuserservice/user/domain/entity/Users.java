package com.example.book2onandonuserservice.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "User")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Setter
    @Column(name = "user_password")
    private String password;

    @Column(name = "user_login_id")
    private String userLoginId;

    @Setter
    @Column(name = "user_name", length = 10)
    @Size(max = 10)
    private String name;

    @Setter
    @Column(name = "user_email", length = 30)
    @Size(max = 30)
    private String email;

    @Setter
    @Column(name = "user_phone", length = 11)
    @Size(max = 11)
    private String phone;

    @Column(name = "user_birth")
    private LocalDate birth;

    @Column(name = "user_created_at")
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "user_latest_at")
    private LocalDateTime lastLoginAt;

    @Setter
    @NotNull
    @Column(name = "user_point")
    private Long point;

    @Setter
    @NotNull
    @Column(name = "user_role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Setter
    @NotNull
    @Column(name = "user_status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Setter
    @Column(name = "user_nickname", length = 10)
    @Size(max = 10)
    private String nickname;

    @Setter
    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_grade_id")
    private UserGrade userGrade;

    //공용 필드
    private void initCommonFields(UserGrade userGrade, String name, String email, String phone, LocalDate birth) {
        this.nickname = "unknown";
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.point = 0L;
        this.role = Role.USER;
        this.status = Status.ACTIVE;

        this.userGrade = userGrade;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birth = birth;
    }

    //로컬 회원가입 생성자
    public Users(String userLoginId, String password, String name, String email, String phone, LocalDate birth,
                 UserGrade userGrade) {
        this.userLoginId = userLoginId;
        this.password = password;

        initCommonFields(userGrade, name, email, phone, birth);
    }

    //소셜 회원가입 생성자
    public Users(String name, String email, String phone, LocalDate birth, UserGrade userGrade) {

        this.userLoginId = null;
        this.password = null;

        initCommonFields(userGrade, name, email, phone, birth);
    }
}
