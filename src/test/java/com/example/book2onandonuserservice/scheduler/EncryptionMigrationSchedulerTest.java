package com.example.book2onandonuserservice.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.scheduler.EncryptionMigrationScheduler;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class EncryptionMigrationSchedulerTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private EncryptionMigrationScheduler scheduler;

    @Mock
    private TransactionStatus transactionStatus;

    @BeforeEach
    void setUp() {
        lenient().doAnswer(invocation -> {
            Consumer<TransactionStatus> action = invocation.getArgument(0);
            action.accept(transactionStatus);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    @DisplayName("마이그레이션 대상 유저가 없을 경우(0명) 즉시 종료")
    void runMigrationTask_noUsers() {
        when(usersRepository.count()).thenReturn(0L);

        scheduler.runMigrationTask();

        verify(usersRepository, never()).findAll(any(PageRequest.class));
        verify(transactionTemplate, never()).executeWithoutResult(any());
    }

    @Test
    @DisplayName("유저가 101명일 때 (배치 사이즈 100) -> 총 2번의 배치(페이지)가 실행되어야 함")
    void runMigrationTask_success_multiple_pages() {
        long totalCount = 101L; // 100명(1페이지) + 1명(2페이지)
        when(usersRepository.count()).thenReturn(totalCount);

        List<Users> page0Users = IntStream.range(0, 100)
                .mapToObj(i -> mock(Users.class))
                .toList();
        Page<Users> page0 = new PageImpl<>(page0Users);

        List<Users> page1Users = List.of(mock(Users.class));
        Page<Users> page1 = new PageImpl<>(page1Users);

        when(usersRepository.findAll(PageRequest.of(0, 100))).thenReturn(page0);
        when(usersRepository.findAll(PageRequest.of(1, 100))).thenReturn(page1);

        scheduler.runMigrationTask();

        verify(transactionTemplate, times(2)).executeWithoutResult(any());
        verify(usersRepository).findAll(PageRequest.of(0, 100));
        verify(usersRepository).findAll(PageRequest.of(1, 100));
        verify(usersRepository, times(101)).save(any(Users.class));
    }

    @Test
    @DisplayName("배치 처리 중 예외 발생 시 롤백 처리하고 다음 배치는 계속 진행")
    void runMigrationTask_exception_handling() {
        when(usersRepository.count()).thenReturn(200L);
        when(usersRepository.findAll(PageRequest.of(0, 100)))
                .thenThrow(new RuntimeException("DB Connection Error"));

        Page<Users> page1 = new PageImpl<>(Collections.emptyList()); // 편의상 빈 리스트
        when(usersRepository.findAll(PageRequest.of(1, 100))).thenReturn(page1);

        scheduler.runMigrationTask();

        verify(transactionStatus, times(1)).setRollbackOnly();
        verify(usersRepository).findAll(PageRequest.of(0, 100));
        verify(usersRepository).findAll(PageRequest.of(1, 100));
    }
}