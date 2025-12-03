package com.example.book2onandonuserservice.auth.service.impl;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoMemberResponse;
import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoTokenResponse;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindIdRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.FindPasswordRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.TokenRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.FindIdResponseDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.RefreshToken;
import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException;
import com.example.book2onandonuserservice.auth.jwt.JwtTokenProvider;
import com.example.book2onandonuserservice.auth.repository.RefreshTokenRepository;
import com.example.book2onandonuserservice.auth.repository.UserAuthRepository;
import com.example.book2onandonuserservice.auth.service.AuthService;
import com.example.book2onandonuserservice.global.client.PaycoClient;
import com.example.book2onandonuserservice.global.config.RabbitConfig;
import com.example.book2onandonuserservice.global.service.EmailService;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.user.domain.dto.response.UserResponseDto;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.UserDormantException;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserLoginIdDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserWithdrawnException;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.net.URI;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UsersRepository usersRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserGradeRepository userGradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PaycoClient paycoClient;
    private final RabbitTemplate rabbitTemplate;
    private final EmailService emailService;
    private final RedisUtil redisUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${payco.client-id}")
    private String paycoClientId;

    @Value("${payco.client-secret}")
    private String paycoClientSecret;

    private static final String LOCAL_PROVIDER = "local";
    private static final String PAYCO_PROVIDER = "payco";

    //로그인 상태 체크 및 마지막 접속일 갱신
    private Users processLogin(Users user) {
        if (user.getStatus() == Status.DORMANT) {
            throw new UserDormantException();
        }
        if (user.getStatus() == Status.CLOSED) {
            throw new UserWithdrawnException();
        }
        user.updateLastLogin();
        return user;
    }

    //토큰 생성
    private TokenResponseDto issueToken(Users user) {
        TokenRequestDto tokenRequest = new TokenRequestDto(user.getUserId(), user.getRole().getKey());
        TokenResponseDto tokenResponse = jwtTokenProvider.createTokens(tokenRequest);

        RefreshToken refreshToken = new RefreshToken(
                String.valueOf(user.getUserId()),
                tokenResponse.refreshToken()
        );
        refreshTokenRepository.save(refreshToken);

        return tokenResponse;
    }

    //기본 등급 조회
    private UserGrade getDefaultGrade() {
        return userGradeRepository.findByGradeName(GradeName.BASIC)
                .orElseThrow(() -> new RuntimeException("기본 등급이 DB에 없습니다."));
    }

    //이메일 인증번호 발송
    @Override
    public void sendVerificationCode(String email) {
        String cleanEmail = email.trim();
        if (usersRepository.findByEmail(cleanEmail).isPresent()) {
            throw new UserEmailDuplicateException();
        }

        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        String key = RedisKeyPrefix.EMAIL_CODE.buildKey(cleanEmail);
        redisUtil.setData(key, code, 5 * 60 * 1000L);

        emailService.sendMail(cleanEmail, "[Book2OnAndOn] 회원가입 인증번호",
                "인증번호는 <b>" + code + "</b> 입니다.");
    }

    //인증번호 검증
    @Override
    public boolean verifyEmail(String email, String code) {
        String cleanEmail = email.trim();
        String codeKey = RedisKeyPrefix.EMAIL_CODE.buildKey(cleanEmail);
        String savedCode = redisUtil.getData(codeKey);

        if (savedCode == null || !savedCode.equals(code)) {
            return false;
        }

        String verifiedKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey(cleanEmail);
        redisUtil.setData(verifiedKey, "true", 30 * 60 * 1000L);
        redisUtil.deleteData(codeKey);
        return true;
    }

    //로컬 회원가입
    @Override
    @Transactional
    public UserResponseDto signUp(LocalSignUpRequestDto request) {
        String cleanEmail = request.email().trim();

        if (usersRepository.existsByUserLoginId(request.userLoginId())) {
            throw new UserLoginIdDuplicateException();
        }
        if (usersRepository.findByEmail(cleanEmail).isPresent()) {
            throw new UserEmailDuplicateException();
        }

        String verifiedKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey(cleanEmail);
        String isVerified = redisUtil.getData(verifiedKey);

        if (!"true".equals(isVerified)) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }
        redisUtil.deleteData(verifiedKey);

        UserGrade defaultGrade = getDefaultGrade();
        String encodePassword = passwordEncoder.encode(request.password());

        Users newUser = new Users(
                request.userLoginId(),
                encodePassword,
                request.name(),
                request.email(),
                request.phone(),
                request.birth(),
                defaultGrade
        );
        Users savedUser = usersRepository.save(newUser);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY,
                    savedUser.getUserId()
            );
        } catch (Exception e) {
            log.warn("RabbitMQ 비활성화 모드 - 메시지 전송 스킵");
        }

        //인증정보 저장
        UserAuth localAuth = UserAuth.builder()
                .provider(LOCAL_PROVIDER)
                .providerUserId(savedUser.getUserLoginId())
                .user(savedUser)
                .build();
        userAuthRepository.save(localAuth);

        return UserResponseDto.fromEntity(savedUser, 0L);
    }

    @Override
    @Transactional
    public TokenResponseDto login(LoginRequestDto request) {
        //인증 정보 조회
        UserAuth userAuth = userAuthRepository.findByProviderAndProviderUserId(LOCAL_PROVIDER, request.userId())
                .orElseThrow(AuthenticationFailedException::new);

        Users user = userAuth.getUser();
        //비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationFailedException();
        }

        //로그인 처리 및 토큰 발급
        Users loginUser = processLogin(user);
        return issueToken(loginUser);
    }

    @Override
    @Transactional
    public TokenResponseDto loginWithPayco(PaycoLoginRequestDto request) {
        //Payco Access Token 발급
        PaycoTokenResponse tokenResponse = paycoClient.getToken(
                "authorization_code",
                paycoClientId,
                paycoClientSecret,
                request.code()
        );

        //Payco 회원 정보 조회
        URI userInfoUri = URI.create("https://apis-payco.krp.toastoven.net/payco/friends/find_member_v2.json");

        PaycoMemberResponse memberResponse = paycoClient.getMemberInfo(
                userInfoUri,
                paycoClientId,
                tokenResponse.accessToken()
        );

        if (!memberResponse.getHeader().isSuccessful()) {
            throw new RuntimeException("PAYCO 로그인 실패: " + memberResponse.getHeader().getResultMessage());
        }

        //Payco 회원정보 추출
        PaycoMemberResponse.PaycoMember paycoInfo = memberResponse.getData().getMember();
        String providerId = paycoInfo.getIdNo();
        String email = paycoInfo.getEmail();
        String name = paycoInfo.getName();
        String phone = parsePhoneNumber(paycoInfo.getMobile());
        LocalDate birth = parseBirthday(paycoInfo.getBirthday());

        //가입여부 확인 및 처리
        Users user = userAuthRepository.findByProviderAndProviderUserId(PAYCO_PROVIDER, providerId)
                .map(UserAuth::getUser)
                .orElseGet(() -> {
                    Users foundOrNewUser = usersRepository.findByEmail(email)
                            .orElseGet(() -> {
                                UserGrade defaultGrade = getDefaultGrade();
                                Users newUser = new Users(name, email, phone, birth, defaultGrade);
                                return usersRepository.save(newUser);
                            });

                    UserAuth newAuth = UserAuth.builder()
                            .provider(PAYCO_PROVIDER)
                            .providerUserId(providerId)
                            .user(foundOrNewUser)
                            .build();
                    userAuthRepository.save(newAuth);

                    return foundOrNewUser;
                });

        processLogin(user);

        return issueToken(user);
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        long expiration = jwtTokenProvider.getExpiration(accessToken);
        long now = System.currentTimeMillis();
        long remainingTime = expiration - now;

        if (remainingTime > 0) {
            String blackListKey = RedisKeyPrefix.BLACKLIST.buildKey(accessToken);
            redisUtil.setBlackList(blackListKey, "logout", remainingTime);
        }
        String userId = jwtTokenProvider.getUserId(accessToken);
        if (userId != null) {
            refreshTokenRepository.deleteById(userId);
            System.out.println("Refresh Token 삭제완료: " + userId);
        }
    }


    //아이디찾기
    @Override
    public FindIdResponseDto findId(FindIdRequestDto request) {
        Users user = usersRepository.findByNameAndEmail(request.name(), request.email())
                .orElseThrow(() -> new RuntimeException("입력하신 정보와 일치하는 회원이 없습니다."));

        //아이디 마스킹 처리
        String maskedId = maskUserId(user.getUserLoginId());

        // 3. 마스킹된 아이디 반환
        return new FindIdResponseDto(maskedId);
    }

    //임시비밀번호 발급 (이메일 전송
    @Override
    @Transactional
    public void issueTemporaryPassword(FindPasswordRequestDto request) {
        Users user = usersRepository.findByUserLoginIdAndEmail(request.userLoginId(), request.email())
                .orElseThrow(() -> new RuntimeException("입력하신 정보와 일치하는 회원이 없습니다."));
        //임시비밀번호 생성
        String tempPassword = generateTempPassword();

        //비밀번호 변경(DB반영)
        String encodedTempPassword = passwordEncoder.encode(tempPassword);
        user.changePassword(encodedTempPassword);

        //이메일 발송
        String subject = "[Book2OnAndOn] 임시비밀번호 안내";
        String text = "회원님의 임시 비밀번호는 <b>" + tempPassword + "</b> 입니다.<br>" +
                "로그인 후 반드시 비밀번호를 변경해 주세요.";
        emailService.sendMail(user.getEmail(), subject, text);
    }


    //Payco 전화번호 파싱
    private String parsePhoneNumber(String paycoMobile) {
        if (paycoMobile == null) {
            return null;
        }

        //- 제거하고 숫자만 남기기
        String cleanNumber = paycoMobile.replaceAll("[^0-9]", "");
        //82(국제번호)로 시작하면 0으로 변경
        if (cleanNumber.startsWith("82")) {
            return "0" + cleanNumber.substring(2);
        }
        return cleanNumber;
    }

    //payco 생일 파싱
    private LocalDate parseBirthday(String paycoBirthday) {
        if (paycoBirthday == null || paycoBirthday.length() != 8) {
            return null;
        }
        return LocalDate.parse(paycoBirthday, java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    //아이디 마스킹
    private String maskUserId(String userLoginId) {
        if (userLoginId == null || userLoginId.isBlank()) {
            return "";
        }
        int length = userLoginId.length();

        return userLoginId.substring(0, length - 2) + "**";
    }


    //임시 비밀번호 생성 로직
    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*#?&";
        SecureRandom random = new SecureRandom();

        while (true) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }

            String pwd = sb.toString();

            if (pwd.matches(".*[A-Za-z].*") &&
                    pwd.matches(".*\\d.*") &&
                    pwd.matches(".*[@$!%*#?&].*")) {
                return pwd;
            }
        }
    }
    /*
    math.random을 사용하면 안됨.
    math.random은 패턴이 존재하고 예측이 가능함.
    OWASP, NIST 등 보안 기준에서 비밀번호·토큰·인증용 난수에 Math.random() 사용 금지
    SecureRandom은 예측이 불가능하고 보안적으로 안전함.
     */

}
