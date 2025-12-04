package com.example.book2onandonuserservice.init;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.init.DataInitializer;
import com.example.book2onandonuserservice.user.domain.entity.GradeName;
import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import com.example.book2onandonuserservice.user.repository.UserGradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {
    @Mock
    private UserGradeRepository userGradeRepository;
    @InjectMocks
    private DataInitializer dataInitializer;

    // 기본 등급이 없을때 등급생성 테스트
    @Test
    void initData_createsGrades_whenBasicNotExists() throws Exception {
        when(userGradeRepository.existsByGradeName(GradeName.BASIC))
                .thenReturn(false);
        CommandLineRunner runner = dataInitializer.initData();
        runner.run();

        verify(userGradeRepository, times(4)).save(any(UserGrade.class));
    }


    //이미 등급이 존재하면 아무것도 실행되지 않는지 검증
    @Test
    void initData_doesNothing_whenBasicExists() throws Exception {
        when(userGradeRepository.existsByGradeName(GradeName.BASIC))
                .thenReturn(true);

        CommandLineRunner runner = dataInitializer.initData();
        runner.run();

        verify(userGradeRepository, never()).save(any(UserGrade.class));
    }
}
