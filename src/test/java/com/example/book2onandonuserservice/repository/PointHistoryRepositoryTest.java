package com.example.book2onandonuserservice.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.book2onandonuserservice.global.config.EncryptionProperties;
import com.example.book2onandonuserservice.global.converter.EncryptStringConverter;
import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class PointHistoryRepositoryTest {

    @MockBean(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @TestConfiguration
    static class TestEncryptionConfig {

        // 1. EncryptionProperties 빈 생성 및 설정
        @Bean
        public EncryptionProperties encryptionProperties() {
            EncryptionProperties props = new EncryptionProperties();
            props.setActiveVersion("v1");
            props.setHashSecret("test-hash-secret");

            Map<String, String> keys = new HashMap<>();
            // AES-256 사용 시 키 길이는 32바이트여야 합니다.
            keys.put("v1", "12345678901234567890123456789012");
            props.setKeys(keys);

            return props;
        }

        // 2. 설정된 Properties를 주입하여 Utils 생성
        @Bean
        public EncryptionUtils encryptionUtils(EncryptionProperties properties) {
            return new EncryptionUtils(properties);
        }

        // 3. EncryptStringConverter 빈 등록
        @Bean
        public EncryptStringConverter encryptStringConverter(EncryptionUtils encryptionUtils) {
            return new EncryptStringConverter(encryptionUtils);
        }
    }

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("sumEarnedInPeriod: 조회 기간 내에 생성된 적립 포인트만 합산된다")
    void sumEarnedInPeriod_works() {

        // given: 기본 등급
        UserGrade basicGrade = new UserGrade(GradeName.BASIC, 0.0, 0);
        em.persist(basicGrade);

        // given: 사용자
        Users user = new Users();
        user.changeGrade(basicGrade);
        user.initSocialAccount("테스트유저", "test-nick");

        ReflectionTestUtils.setField(user, "userLoginId", "login123");
        ReflectionTestUtils.setField(user, "password", "pw1234");
        ReflectionTestUtils.setField(user, "email", "test@example.com");
        // [수정] 해시 필드 강제 주입
        ReflectionTestUtils.setField(user, "emailHash", "hashed_test@example.com");

        ReflectionTestUtils.setField(user, "phone", "01088889999");
        ReflectionTestUtils.setField(user, "birth", LocalDate.of(2025, 12, 1));

        em.persist(user);

        LocalDateTime base = LocalDateTime.of(2025, 12, 1, 0, 0);

        // 기간 내 적립 100
        em.persist(PointHistory.builder()
                .user(user)
                .pointHistoryChange(100)
                .totalPoints(100)
                .remainingPoint(100)
                .pointCreatedDate(base.plusDays(1))
                .pointExpiredDate(base.plusYears(1))
                .pointReason(PointReason.ORDER)
                .build());

        // 기간 내 적립 200
        em.persist(PointHistory.builder()
                .user(user)
                .pointHistoryChange(200)
                .totalPoints(300)
                .remainingPoint(300)
                .pointCreatedDate(base.plusDays(2))
                .pointExpiredDate(base.plusYears(1))
                .pointReason(PointReason.ORDER)
                .build());

        // 기간 이전 적립 300 (집계 제외 대상)
        em.persist(PointHistory.builder()
                .user(user)
                .pointHistoryChange(300)
                .totalPoints(600)
                .remainingPoint(600)
                .pointCreatedDate(base.minusDays(1))
                .pointExpiredDate(base.plusYears(1))
                .pointReason(PointReason.ORDER)
                .build());

        em.flush();

        // when
        int sum = pointHistoryRepository.sumEarnedInPeriod(
                user.getUserId(),
                base,
                base.plusDays(10)
        );

        // then: 기간 내 적립분(100 + 200)만 합산
        assertThat(sum).isEqualTo(300);
    }

    @Test
    @DisplayName("findLatestForUpdateOne: 내역이 없으면 Optional.empty 반환")
    void findLatestForUpdateOne_empty() {
        // given: 사용자만 만들고 pointHistory는 저장 안 함
        UserGrade basicGrade = new UserGrade(GradeName.BASIC, 0.0, 0);
        em.persist(basicGrade);

        Users user = new Users();
        user.changeGrade(basicGrade);
        user.initSocialAccount("테스트유저", "test-nick");
        ReflectionTestUtils.setField(user, "userLoginId", "login123");
        ReflectionTestUtils.setField(user, "password", "pw1234");
        ReflectionTestUtils.setField(user, "email", "test@example.com");
        // [수정] 해시 필드 강제 주입
        ReflectionTestUtils.setField(user, "emailHash", "hashed_test@example.com");

        ReflectionTestUtils.setField(user, "phone", "01088889999");
        ReflectionTestUtils.setField(user, "birth", LocalDate.of(2025, 12, 1));
        em.persist(user);

        em.flush();

        // when
        var res = pointHistoryRepository.findLatestForUpdateOne(user.getUserId());

        // then
        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("findLatestForUpdateOne: 내역이 있으면 최신 1건 반환")
    void findLatestForUpdateOne_returnsLatest() {
        // given
        UserGrade basicGrade = new UserGrade(GradeName.BASIC, 0.0, 0);
        em.persist(basicGrade);

        Users user = new Users();
        user.changeGrade(basicGrade);
        user.initSocialAccount("테스트유저", "test-nick");
        ReflectionTestUtils.setField(user, "userLoginId", "login123");
        ReflectionTestUtils.setField(user, "password", "pw1234");
        ReflectionTestUtils.setField(user, "email", "test@example.com");
        ReflectionTestUtils.setField(user, "emailHash", "hashed_test@example.com");

        ReflectionTestUtils.setField(user, "phone", "01088889999");
        ReflectionTestUtils.setField(user, "birth", LocalDate.of(2025, 12, 1));
        em.persist(user);

        LocalDateTime base = LocalDateTime.of(2025, 12, 1, 0, 0);

        // 오래된 것
        em.persist(PointHistory.builder()
                .user(user)
                .pointHistoryChange(100)
                .totalPoints(100)
                .remainingPoint(100)
                .pointCreatedDate(base.plusDays(1))
                .pointExpiredDate(base.plusYears(1))
                .pointReason(PointReason.ORDER)
                .build());

        // 최신 것
        em.persist(PointHistory.builder()
                .user(user)
                .pointHistoryChange(50)
                .totalPoints(150)
                .remainingPoint(150)
                .pointCreatedDate(base.plusDays(2))
                .pointExpiredDate(base.plusYears(1))
                .pointReason(PointReason.ORDER)
                .build());

        em.flush();

        // when
        var res = pointHistoryRepository.findLatestForUpdateOne(user.getUserId());

        // then
        assertThat(res).isPresent();
        assertThat(res.get().getTotalPoints()).isEqualTo(150);
    }
}