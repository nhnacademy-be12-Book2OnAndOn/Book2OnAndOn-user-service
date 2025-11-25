package com.example.book2onandonuserservice.auth.service.impl;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoMemberResponse;
import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoTokenResponse;
import com.example.book2onandonuserservice.auth.domain.dto.request.LocalSignUpRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.request.TokenRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException;
import com.example.book2onandonuserservice.auth.jwt.JwtTokenProvider;
import com.example.book2onandonuserservice.auth.repository.UserAuthRepository;
import com.example.book2onandonuserservice.auth.service.AuthService;
import com.example.book2onandonuserservice.global.client.PaycoClient;
import com.example.book2onandonuserservice.global.service.CouponService;
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
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private final UsersRepository usersRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserGradeRepository userGradeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PaycoClient paycoClient;
    private final CouponService couponService;

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
        TokenRequestDto tokenRequest = new TokenRequestDto(user.getUserId(), user.getRole().name());
        return jwtTokenProvider.createTokens(tokenRequest);
    }

    //기본 등급 조회
    private UserGrade getDefaultGrade() {
        String basicGrade = GradeName.BASIC.name();
        return userGradeRepository.findByGradeName(GradeName.BASIC)
                .orElseThrow(() -> new RuntimeException(basicGrade + "가 DB에 없습니다."));
    }

    //로컬 회원가입
    @Override
    @Transactional
    public UserResponseDto signUp(LocalSignUpRequestDto request) {
        if (usersRepository.existsByUserLoginId(request.userLoginId())) {
            throw new UserLoginIdDuplicateException();
        }
        if (usersRepository.findByEmail(request.email()).isPresent()) {
            throw new UserEmailDuplicateException();
        }

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
        couponService.issueWelcomeCoupon(savedUser.getUserId());

        //인증정보 저장
        UserAuth localAuth = UserAuth.builder()
                .provider(LOCAL_PROVIDER)
                .providerUserId(savedUser.getUserLoginId())
                .user(savedUser)
                .build();
        userAuthRepository.save(localAuth);

        return UserResponseDto.fromEntity(savedUser);
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
        Optional<UserAuth> userAuthOpt = userAuthRepository.findByProviderAndProviderUserId(
                PAYCO_PROVIDER, providerId);

        Users user;
        if (userAuthOpt.isPresent()) {
            user = userAuthOpt.get().getUser();
            user = processLogin(user);

        } else {
            Optional<Users> userOptByEmail = usersRepository.findByEmail(email);

            if (userOptByEmail.isPresent()) {
                user = userOptByEmail.get();
                user = processLogin(user);
            } else {
                UserGrade defaultGrade = getDefaultGrade();
                user = new Users(
                        name,
                        email,
                        phone,
                        birth,
                        defaultGrade
                );
                usersRepository.save(user);
            }

            UserAuth newAuth = UserAuth.builder()
                    .provider(PAYCO_PROVIDER)
                    .providerUserId(providerId)
                    .user(user)
                    .build();
            userAuthRepository.save(newAuth);
        }

        return issueToken(user);
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
}
