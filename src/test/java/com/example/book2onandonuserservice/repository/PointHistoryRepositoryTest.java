package com.example.book2onandonuserservice.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import com.example.book2onandonuserservice.point.domain.entity.PointHistory;
import com.example.book2onandonuserservice.point.domain.entity.PointReason;
import com.example.book2onandonuserservice.point.repository.PointHistoryRepository;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@DataJpaTest
class PointHistoryRepositoryTest {

    @TestConfiguration
    static class TestEncryptionConfig {
        @Bean
        public EncryptionUtils encryptionUtils() {
            return new EncryptionUtils("test-secret-key");
        }
    }

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void sumEarnedInPeriod_works() {

        UserGrade basicGrade = new UserGrade(GradeName.BASIC, 0.0, 0);
        em.persist(basicGrade);

        Users user = new Users();
        user.changeGrade(basicGrade);
        user.initSocialAccount("테스트유저", "test-nick");
        em.persist(user);

        LocalDateTime base = LocalDateTime.of(2025, 12, 1, 0, 0);

        em.persist(PointHistory.builder()
                .user(user)
                .pointHistoryChange(100)
                .totalPoints(100)
                .remainingPoint(100)
                .pointCreatedDate(base.plusDays(1))
                .pointExpiredDate(base.plusYears(1))
                .pointReason(PointReason.ORDER)
                .build());

        em.persist(PointHistory.builder()
                .user(user)
                .pointHistoryChange(200)
                .totalPoints(300)
                .remainingPoint(300)
                .pointCreatedDate(base.plusDays(2))
                .pointExpiredDate(base.plusYears(1))
                .pointReason(PointReason.ORDER)
                .build());

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

        int sum = pointHistoryRepository.sumEarnedInPeriod(
                user.getUserId(),
                base,
                base.plusDays(10)
        );

        assertThat(sum).isEqualTo(300);
    }
}
