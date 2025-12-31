package com.example.book2onandonuserservice.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.config.DataMigrationRunner;
import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class DataMigrationRunnerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EncryptionUtils encryptionUtils;

    @InjectMocks
    private DataMigrationRunner dataMigrationRunner;

    @Test
    @DisplayName("모든 데이터가 평문일 때 정상적으로 암호화 및 업데이트 수행")
    void run_migrates_all_plaintext_data() {
        List<Map<String, Object>> users = new ArrayList<>();
        Map<String, Object> user = new HashMap<>();
        user.put("user_id", 1L);
        user.put("user_email", "test@example.com"); // 평문 (@ 포함)
        user.put("user_phone", "010-1234-5678");    // 평문 (- 포함)
        users.add(user);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(users);
        when(encryptionUtils.encrypt("test@example.com")).thenReturn("v2:encryptedEmail");
        when(encryptionUtils.hash("test@example.com")).thenReturn("hashedEmail");
        when(encryptionUtils.encrypt("010-1234-5678")).thenReturn("v2:encryptedPhone");

        dataMigrationRunner.run();

        verify(jdbcTemplate, times(1)).update(
                "UPDATE users SET user_email = ?, user_email_hash = ?, user_phone = ? WHERE user_id = ?",
                "v2:encryptedEmail",
                "hashedEmail",
                "v2:encryptedPhone",
                1L
        );
    }

    @Test
    @DisplayName("이미 암호화된 데이터는 업데이트하지 않음")
    void run_skips_already_encrypted_data() {
        List<Map<String, Object>> users = new ArrayList<>();
        Map<String, Object> user = new HashMap<>();
        user.put("user_id", 2L);
        user.put("user_email", "v2:encryptedEmail"); // 이미 암호화됨 (@ 없음)
        user.put("user_phone", "v2:encryptedPhone"); // 이미 암호화됨 (- 없고 숫자만 있지 않음)
        users.add(user);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(users);

        dataMigrationRunner.run();

        verify(encryptionUtils, never()).encrypt(anyString());
        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    @DisplayName("전화번호만 평문인 경우 (이메일은 이미 암호화됨)")
    void run_migrates_only_phone() {
        List<Map<String, Object>> users = new ArrayList<>();
        Map<String, Object> user = new HashMap<>();
        user.put("user_id", 3L);
        user.put("user_email", "v2:encryptedEmail"); // 암호화됨
        user.put("user_phone", "01012345678");       // 평문 (숫자로만 구성)
        users.add(user);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(users);
        when(encryptionUtils.encrypt("01012345678")).thenReturn("v2:encryptedPhone");

        dataMigrationRunner.run();

        verify(jdbcTemplate).update(
                "UPDATE users SET user_email = ?, user_phone = ? WHERE user_id = ?",
                "v2:encryptedEmail", // 기존 값 유지
                "v2:encryptedPhone", // 변경된 값
                3L
        );
    }

    @Test
    @DisplayName("이메일만 평문인 경우 (전화번호는 이미 암호화됨)")
    void run_migrates_only_email() {
        List<Map<String, Object>> users = new ArrayList<>();
        Map<String, Object> user = new HashMap<>();
        user.put("user_id", 4L);
        user.put("user_email", "update@me.com");    // 평문
        user.put("user_phone", "v2:encryptedPhone"); // 암호화됨
        users.add(user);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(users);
        when(encryptionUtils.encrypt("update@me.com")).thenReturn("v2:newEncryptedEmail");
        when(encryptionUtils.hash("update@me.com")).thenReturn("newHash");
        dataMigrationRunner.run();

        verify(jdbcTemplate).update(
                "UPDATE users SET user_email = ?, user_email_hash = ?, user_phone = ? WHERE user_id = ?",
                "v2:newEncryptedEmail",
                "newHash",
                "v2:encryptedPhone",
                4L
        );
    }

    @Test
    @DisplayName("데이터가 null인 경우 안전하게 스킵")
    void run_handles_null_values() {
        List<Map<String, Object>> users = new ArrayList<>();
        Map<String, Object> user = new HashMap<>();
        user.put("user_id", 5L);
        user.put("user_email", null);
        user.put("user_phone", null);
        users.add(user);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(users);

        dataMigrationRunner.run();

        verify(encryptionUtils, never()).encrypt(any());
        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    @DisplayName("암호화 중 예외 발생 시 해당 필드 업데이트 스킵")
    void run_handles_encryption_exception() {
        List<Map<String, Object>> users = new ArrayList<>();
        Map<String, Object> user = new HashMap<>();
        user.put("user_id", 6L);
        user.put("user_email", "error@email.com");
        user.put("user_phone", "010-error");
        users.add(user);

        when(jdbcTemplate.queryForList(anyString())).thenReturn(users);
        when(encryptionUtils.encrypt("error@email.com")).thenThrow(new RuntimeException("Email Enc Error"));
        when(encryptionUtils.encrypt("010-error")).thenThrow(new RuntimeException("Phone Enc Error"));

        dataMigrationRunner.run();

        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }

    @Test
    @DisplayName("사용자 목록이 비어있을 때 정상 종료")
    void run_empty_list() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(Collections.emptyList());

        dataMigrationRunner.run();

        verify(jdbcTemplate, never()).update(anyString(), any(Object[].class));
    }
}