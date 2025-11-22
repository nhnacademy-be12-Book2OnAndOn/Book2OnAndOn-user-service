package com.example.book2onandonuserservice.global.init;

import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserGradeRepository userGradeRepository;

    //테스트 properties 사용해서 서버 실행시 자동으로 등급 생성
    @Bean
    @Profile("test")
    public CommandLineRunner initData() {
        return args -> {
            if (!userGradeRepository.existsByGradeName(GradeName.BASIC)) {
                UserGrade basic = new UserGrade(GradeName.BASIC, 0.01, 0);
                userGradeRepository.save(basic);

                userGradeRepository.save(new UserGrade(GradeName.ROYAL, 0.02, 100000));
                userGradeRepository.save(new UserGrade(GradeName.GOLD, 0.025, 200000));
                userGradeRepository.save(new UserGrade(GradeName.PLATINUM, 0.03, 300000));

                System.out.println("=============== [Init] 테스트용 등급 데이터 생성 완료 ===============");
            }
        };
    }
}