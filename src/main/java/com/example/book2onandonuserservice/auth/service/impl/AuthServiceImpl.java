package com.example.book2onandonuserservice.auth.service.impl;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindIdRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindPasswordRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.ReissueRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.FindIdResponseDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException;
import com.example.book2onandonuserservice.auth.repository.jpa.UserAuthRepository;
import com.example.book2onandonuserservice.auth.service.AuthService;
import com.example.book2onandonuserservice.auth.service.AuthTokenService;
import com.example.book2onandonuserservice.auth.service.AuthVerificationService;
import com.example.book2onandonuserservice.auth.service.PaycoAuthService;
import com.example.book2onandonuserservice.global.annotation.DistributedLock;
import com.example.book2onandonuserservice.global.event.EmailSendEvent;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.EmailNotVerifiedException;
import com.example.book2onandonuserservice.user.exception.UserDormantException;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserLoginIdDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserNicknameDuplicationException;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import com.example.book2onandonuserservice.user.exception.UserWithdrawnException;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthTokenService authTokenService;       // 토큰 발급/관리
    private final AuthVerificationService verificationService; // 이메일/휴면 인증
    private final PaycoAuthService paycoAuthService;       // PAYCO 로그인

    private final UsersRepository usersRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserGradeRepository userGradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final PointHistoryService pointHistoryService;
    private final RedisUtil redisUtil;
    private final ApplicationEventPublisher eventPublisher;

    private static final String LOCAL_PROVIDER = "local";
    private static final SecureRandom secureRandom = new SecureRandom();


    // 위임 메서드
    @Override
    public void sendVerificationCode(String email) {
        verificationService.sendVerificationCode(email);
    }

    @Override
    public void sendDormantVerificationCode(String email) {
        verificationService.sendDormantVerificationCode(email);
    }

    @Override
    public boolean verifyEmail(String email, String code) {
        return verificationService.verifyEmail(email, code);
    }

    @Override
    @Transactional
    public void unlockDormantAccount(String email, String code) {
        verificationService.unlockDormantAccount(email, code);
    }

    @Override
    @Transactional
    public TokenResponseDto loginWithPayco(PaycoLoginRequestDto request) {
        return paycoAuthService.login(request);
    }

    @Override
    public void logout(String accessToken) {
        authTokenService.logout(accessToken);
    }

    @Override
    @DistributedLock(key = "'REISSUE_TOKEN:' + #request.refreshToken") //RefreshToken 값을 기준으로 락 설정
    @Transactional // 순서: Lock 획득 -> Transaction 시작 -> 로직 수행 -> Transaction 커밋 -> Lock 해제
    public TokenResponseDto reissue(ReissueRequestDto request) {
        return authTokenService.reissueToken(request);
    }

    // 로컬 인증 로직

    // 로컬 회원가입
    @Transactional
    @Override
    public UserResponseDto signUp(LocalSignUpRequestDto request) {
        String cleanEmail = request.email().trim();

        if (usersRepository.existsByUserLoginId(request.userLoginId())) {
            throw new UserLoginIdDuplicateException();
        }
        if (usersRepository.existsByNickname(request.nickname())) {
            throw new UserNicknameDuplicationException(); // "이미 사용중인 닉네임 입니다." 메시지 포함됨
        }
        if (usersRepository.findByEmail(cleanEmail).isPresent()) {
            throw new UserEmailDuplicateException();
        }

        String verifiedKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey(cleanEmail);
        String isVerified = redisUtil.getData(verifiedKey);

        if (!"true".equals(isVerified)) {
            throw new EmailNotVerifiedException();
        }
        redisUtil.deleteData(verifiedKey);

        // 등급 조회 및 비밀번호 암호화
        UserGrade defaultGrade = getDefaultGrade();
        String encodedPassword = passwordEncoder.encode(request.password());

        // 유저 저장
        Users newUser = new Users();

        newUser.initLocalAccount(
                request.userLoginId(),
                encodedPassword,
                request.name(),
                request.nickname()
        );

        newUser.setContactInfo(
                request.email(),
                request.phone(),
                request.birth()
        );

        newUser.changeGrade(defaultGrade);

        Users savedUser = usersRepository.save(newUser);

        // 회원가입 포인트 적립
        try {
            pointHistoryService.earnSignupPoint(savedUser.getUserId());
            log.info("로컬 회원가입 포인트 적립 완료: userId={}", savedUser.getUserId());
        } catch (Exception e) {
            log.warn("로컬 회원가입 포인트 적립 실패: {}", e.getMessage());
        }
        // 인증 정보 저장
        UserAuth localAuth = UserAuth.builder()
                .provider(LOCAL_PROVIDER)
                .providerUserId(savedUser.getUserLoginId())
                .user(savedUser)
                .build();
        userAuthRepository.save(localAuth);

        return UserResponseDto.fromEntity(savedUser);
    }

    // 로컬 로그인
    @Override
    @Transactional
    public TokenResponseDto login(LoginRequestDto request) {
        // 인증 정보 조회
        UserAuth userAuth = userAuthRepository.findByProviderAndProviderUserId(LOCAL_PROVIDER, request.userId())
                .orElseThrow(AuthenticationFailedException::new);

        Users user = userAuth.getUser();

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationFailedException();
        }

        // 상태 체크 및 로그인 처리
        processLogin(user);

        // 토큰 발급 (AuthTokenService 위임)
        return authTokenService.issueToken(user);
    }

    // 아이디 찾기
    @Override
    public FindIdResponseDto findMemberIdByNameAndEmail(FindIdRequestDto request) {
        Users user = usersRepository.findByNameAndEmail(request.name(), request.email())
                .orElseThrow(() -> new UserNotFoundException("입력하신 정보와 일치하는 회원이 없습니다."));

        String maskedId = maskUserId(user.getUserLoginId());
        return new FindIdResponseDto(maskedId);
    }

    // 임시 비밀번호 발급
    @Override
    @Transactional
    public void issueTemporaryPassword(FindPasswordRequestDto request) {
        Users user = usersRepository.findByUserLoginIdAndEmail(request.userLoginId(), request.email())
                .orElseThrow(() -> new UserNotFoundException("입력하신 정보와 일치하는 회원이 없습니다."));

        String tempPassword = generateTempPassword();
        String encodedTempPassword = passwordEncoder.encode(tempPassword);

        user.changePassword(encodedTempPassword);

        // 이메일 발송 이벤트 발행 (비동기)
        String subject = "[Book2OnAndOn] 임시비밀번호 안내";
        String text = "회원님의 임시 비밀번호는 <b>" + tempPassword + "</b> 입니다.<br>" +
                "로그인 후 반드시 비밀번호를 변경해 주세요.";

        eventPublisher.publishEvent(new EmailSendEvent(user.getEmail(), subject, text));
    }


    // 헬퍼 메서드
    private void processLogin(Users user) {
        if (user.getStatus() == Status.DORMANT) {
            throw new UserDormantException();
        }
        if (user.getStatus() == Status.CLOSED) {
            throw new UserWithdrawnException();
        }
        user.updateLastLogin();
    }

    private UserGrade getDefaultGrade() {
        return userGradeRepository.findByGradeName(GradeName.BASIC)
                .orElseThrow(() -> new IllegalStateException("데이터베이스에 기본 회원 등급(BASIC)이 존재하지 않습니다. 관리자에게 문의하세요."));
    }

    private String maskUserId(String userLoginId) {
        if (userLoginId == null || userLoginId.isBlank()) {
            return "";
        }
        int length = userLoginId.length();
        if (length <= 2) {
            return userLoginId;
        }
        return userLoginId.substring(0, length - 2) + "**";
    }

    private String generateTempPassword() {
        String specialCharsStr = "@$!%*#?&";
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*#?&";

        while (true) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
            }
            String pwd = sb.toString();
            if (pwd.chars().anyMatch(Character::isLetter) &&
                    pwd.chars().anyMatch(Character::isDigit) &&
                    pwd.chars().anyMatch(ch -> specialCharsStr.indexOf(ch) >= 0)) {
                return pwd;
            }
        }
    }
}