package com.example.book2onandonuserservice.user.domain.entity;

import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.global.converter.EncryptStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_password", length = 255, nullable = false)
    private String password;

    @Column(name = "user_login_id", unique = true, length = 30, nullable = false)
    private String userLoginId;

    @Column(name = "user_name", length = 50, nullable = false)
    @Size(max = 50)
    private String name;

    @Column(name = "user_email", unique = true, length = 500, nullable = false)
    private String email;

    @Column(name = "user_phone", length = 255, nullable = false)
    @Convert(converter = EncryptStringConverter.class)
    private String phone;

    @Column(name = "user_birth", nullable = false)
    private LocalDate birth;

    @Column(name = "user_created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_latest_login", nullable = false)
    private LocalDateTime lastLoginAt;

    @NotNull
    @Column(name = "user_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    @Column(name = "user_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "user_nickname", unique = true, length = 20, nullable = false)
    private String nickname;

    @Column(name = "user_withdrawn_at")
    private LocalDateTime withdrawnAt;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_grade_id", nullable = false)
    private UserGrade userGrade;

    @Column(name = "withdraw_reason")
    private String withdrawReason;

    @OneToMany(mappedBy = "user")
    private List<UserAuth> userAuths = new ArrayList<>();

    //생성자
    private void initDefaults(String name, String nickname) {
        this.name = name;
        this.nickname = nickname;
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.role = Role.USER;
        this.status = Status.ACTIVE;
    }

    public void initLocalAccount(String userLoginId, String password, String name, String nickname) {
        this.userLoginId = userLoginId;
        this.password = password;
        initDefaults(name, nickname);
    }

    public void initSocialAccount(String name, String nickname) {
        initDefaults(name, nickname);
        this.userLoginId = "SOC_" + UUID.randomUUID().toString().substring(0, 20);
        this.password = UUID.randomUUID().toString();
    }

    public void setContactInfo(String email, String phone, LocalDate birth) {
        this.email = email;
        this.phone = phone;
        this.birth = birth;
    }

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
    public void withDraw(String reason) {
        this.status = Status.CLOSED;
        this.withdrawnAt = LocalDateTime.now();
        this.name = "탈퇴회원";
        this.email = "withdrawn_" + this.userId + "@deleted.com";
        this.phone = null;
        this.withdrawReason = reason;
    }

    //등급 변경
    public void changeGrade(UserGrade userGrade) {
        this.userGrade = userGrade;
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    public void changeStatus(Status newStatus) {
        this.status = newStatus;
    }
}
