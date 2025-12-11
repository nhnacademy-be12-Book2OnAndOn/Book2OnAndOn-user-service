package com.example.book2onandonuserservice.auth.service;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoLoginRequestDto;
import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoMemberResponse;
import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoTokenResponse;
import com.example.book2onandonuserservice.auth.domain.dto.response.TokenResponseDto;
import com.example.book2onandonuserservice.auth.domain.entity.UserAuth;
import com.example.book2onandonuserservice.auth.exception.PaycoInfoMissingException;
import com.example.book2onandonuserservice.auth.exception.PaycoServerException;
import com.example.book2onandonuserservice.auth.repository.UserAuthRepository;
import com.example.book2onandonuserservice.global.client.PaycoClient;
import com.example.book2onandonuserservice.point.service.PointHistoryService;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.UserDormantException;
import com.example.book2onandonuserservice.user.exception.UserWithdrawnException;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaycoAuthService {

    private final PaycoClient paycoClient;
    private final UserAuthRepository userAuthRepository;
    private final UsersRepository usersRepository;
    private final UserGradeRepository userGradeRepository;
    private final PointHistoryService pointHistoryService;

    private final AuthTokenService authTokenService;

    @Value("${payco.client-id}")
    private String paycoClientId;

    @Value("${payco.client-secret}")
    private String paycoClientSecret;

    private static final String PAYCO_PROVIDER = "payco";

    //페이코 로그인
    @Transactional
    public TokenResponseDto login(PaycoLoginRequestDto request) {
        // Payco Access Token 발급
        PaycoTokenResponse tokenResponse = paycoClient.getToken(
                "authorization_code",
                paycoClientId,
                paycoClientSecret,
                request.code()
        );

        // Payco 회원 정보 조회
        URI userInfoUri = URI.create("https://apis-payco.krp.toastoven.net/payco/friends/find_member_v2.json");
        PaycoMemberResponse memberResponse = paycoClient.getMemberInfo(
                userInfoUri,
                paycoClientId,
                tokenResponse.accessToken()
        );

        if (!memberResponse.getHeader().isSuccessful()) {
            throw new PaycoServerException("PAYCO 로그인 실패: " + memberResponse.getHeader().getResultMessage());
        }

        PaycoMemberResponse.PaycoMember paycoInfo = memberResponse.getData().getMember();
        String providerId = paycoInfo.getIdNo();
        String email = paycoInfo.getEmail();
        String name = paycoInfo.getName();
        String phone = parsePhoneNumber(paycoInfo.getMobile());
        LocalDate birth = parseBirthday(paycoInfo.getBirthday());

        Optional<UserAuth> existingAuth = userAuthRepository.findByProviderAndProviderUserId(PAYCO_PROVIDER,
                providerId);

        if (existingAuth.isPresent()) {
            Users user = existingAuth.get().getUser();
            validateUserStatus(user);
            user.updateLastLogin();
            return authTokenService.issueToken(user);
        }

        if (!StringUtils.hasText(email) || !StringUtils.hasText(name)) {
            throw new PaycoInfoMissingException();
        }

        // 신규 가입 또는 기존 계정 연동
        Users user = usersRepository.findByEmail(email)
                .orElseGet(() -> createNewUser(name, email, phone, birth));

        // PAYCO 연동 정보 저장
        linkUserWithPayco(user, providerId);

        // 8. 로그인 처리 및 토큰 발급
        validateUserStatus(user);
        user.updateLastLogin();

        return authTokenService.issueToken(user);
    }

    // === Private Helper Methods ===

    private Users createNewUser(String name, String email, String phone, LocalDate birth) {
        UserGrade defaultGrade = userGradeRepository.findByGradeName(GradeName.BASIC)
                .orElseThrow(() -> new IllegalStateException("기본 회원 등급(BASIC)이 존재하지 않습니다."));

        Users newUser = new Users();
        newUser.initSocialAccount(name, name);
        newUser.setContactInfo(email, phone, birth);
        newUser.changeGrade(defaultGrade);
        Users savedUser = usersRepository.save(newUser);

        // 회원가입 포인트 적립
        try {
            pointHistoryService.earnSignupPoint(savedUser.getUserId());
            log.info("PAYCO 회원가입 포인트 적립 완료: userId={}", savedUser.getUserId());
        } catch (Exception e) {
            log.warn("PAYCO 회원가입 포인트 적립 실패: {}", e.getMessage());
        }
        return savedUser;
    }

    private void linkUserWithPayco(Users user, String providerId) {
        UserAuth newAuth = UserAuth.builder()
                .provider(PAYCO_PROVIDER)
                .providerUserId(providerId)
                .user(user)
                .build();
        userAuthRepository.save(newAuth);
    }

    private void validateUserStatus(Users user) {
        if (user.getStatus() == Status.DORMANT) {
            throw new UserDormantException();
        }
        if (user.getStatus() == Status.CLOSED) {
            throw new UserWithdrawnException();
        }
    }

    private String parsePhoneNumber(String paycoMobile) {
        if (paycoMobile == null) {
            return null;
        }
        String cleanNumber = paycoMobile.replaceAll("\\D", "");
        if (cleanNumber.startsWith("82")) {
            return "0" + cleanNumber.substring(2);
        }
        return cleanNumber;
    }

    private LocalDate parseBirthday(String paycoBirthday) {
        if (paycoBirthday == null || paycoBirthday.length() != 8) {
            return null;
        }
        return LocalDate.parse(paycoBirthday, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}