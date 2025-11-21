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

    @Column(name = "user_password", length = 255)
    private String password;

    @Column(name = "user_login_id", unique = true, length = 30)
    private String userLoginId;

    @Column(name = "user_name", length = 50)
    @Size(max = 50)
    private String name;

    @Column(name = "user_email", unique = true, length = 320)
    private String email;

    @Column(name = "user_phone", length = 11)
    private String phone;

    @Column(name = "user_birth")
    private LocalDate birth;

    @Column(name = "user_created_at")
    private LocalDateTime createdAt;

    @Column(name = "user_latest_login")
    private LocalDateTime lastLoginAt;

    @NotNull
    @Column(name = "user_role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    @Column(name = "user_status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "user_nickname", unique = true, length = 20, nullable = false)
    private String nickname;

    @Column(name = "user_withdrawn_at")
    private LocalDateTime withdrawnAt;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_grade_id")
    private UserGrade userGrade;

    //생성자
    private void initCommonFields(UserGrade userGrade, String name, String email, String phone, LocalDate birth) {
        this.nickname = name;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.role = Role.USER;
        this.status = Status.ACTIVE;
        this.userGrade = userGrade;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birth = birth;
    }

    public Users(String userLoginId, String password, String name, String email, String phone, LocalDate birth,
                 UserGrade userGrade) {
        this.userLoginId = userLoginId;
        this.password = password;
        initCommonFields(userGrade, name, email, phone, birth);
    }

    public Users(String name, String email, String phone, LocalDate birth, UserGrade userGrade) {
        this.userLoginId = null;
        this.password = null;
        initCommonFields(userGrade, name, email, phone, birth);
    }

    //비즈니스 로직 더티체킹
    //프로필 정보 수정
    public void updateProfile(String name, String email, String nickname, String phone) {
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.phone = phone;
    }

    //비밀번호 변경
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    //로그인 시간 갱신
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    //회원 탈퇴
    public void withDraw() {
        this.status = Status.CLOSED;
        this.withdrawnAt = LocalDateTime.now();
        this.name = "탈퇴회원";
        this.email = "withdrawn_" + this.userId; //Unique 제약조건을 피하기 위함
        this.phone = null;
        this.email = null;
    }


}
