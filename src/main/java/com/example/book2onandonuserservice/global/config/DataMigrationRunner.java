package com.example.book2onandonuserservice.global.config;

import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final EncryptionUtils encryptionUtils;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=========== [데이터 암호화 마이그레이션 시작] ===========");

        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT user_id, user_email, user_phone FROM users");
        int updatedCount = 0;

        for (Map<String, Object> user : users) {
            if (migrateUser(user)) {
                updatedCount++;
            }
        }

        log.info("=========== [마이그레이션 완료] 총 {}건 업데이트 됨 ===========", updatedCount);
    }

    /**
     * 개별 사용자 처리 로직
     *
     * @return 업데이트가 발생했으면 true, 아니면 false
     */
    private boolean migrateUser(Map<String, Object> user) {
        Long userId = (Long) user.get("user_id");
        String rawEmail = (String) user.get("user_email");
        String rawPhone = (String) user.get("user_phone");

        MigrationResult emailResult = processEmail(userId, rawEmail);
        String encryptedPhone = processPhone(userId, rawPhone);

        // 둘 다 null이어도 true를 반환하여 NPE 방지
        if (!emailResult.isChanged && Objects.equals(encryptedPhone, rawPhone)) {
            return false;
        }

        updateUser(userId, emailResult, encryptedPhone);
        return true;
    }

    private MigrationResult processEmail(Long userId, String rawEmail) {
        // 이메일 암호화 체크: '@'가 포함되어 있으면 평문으로 간주
        if (rawEmail != null && rawEmail.contains("@")) {
            try {
                String trimmedEmail = rawEmail.trim();
                return new MigrationResult(
                        encryptionUtils.encrypt(trimmedEmail),
                        encryptionUtils.hash(trimmedEmail),
                        true
                );
            } catch (Exception e) {
                log.error("이메일 암호화 실패 (ID: {}): {}", userId, e.getMessage());
            }
        }
        // 변경 없음
        return new MigrationResult(rawEmail, null, false);
    }

    private String processPhone(Long userId, String rawPhone) {
        // 전화번호 암호화 체크: '-'가 있거나 숫자로만 되어있으면 평문으로 간주
        if (rawPhone != null && (rawPhone.contains("-") || rawPhone.matches("^\\d+$"))) {
            try {
                return encryptionUtils.encrypt(rawPhone);
            } catch (Exception e) {
                log.error("전화번호 암호화 실패 (ID: {}): {}", userId, e.getMessage());
            }
        }
        return rawPhone;
    }

    private void updateUser(Long userId, MigrationResult emailResult, String encryptedPhone) {
        if (emailResult.emailHash != null) {
            jdbcTemplate.update(
                    "UPDATE users SET user_email = ?, user_email_hash = ?, user_phone = ? WHERE user_id = ?",
                    emailResult.email, emailResult.emailHash, encryptedPhone, userId);
        } else {
            jdbcTemplate.update(
                    "UPDATE users SET user_email = ?, user_phone = ? WHERE user_id = ?",
                    emailResult.email, encryptedPhone, userId);
        }
        log.info("Migrated User ID: {}", userId);
    }

    private static class MigrationResult {
        String email;
        String emailHash;
        boolean isChanged;

        public MigrationResult(String email, String emailHash, boolean isChanged) {
            this.email = email;
            this.emailHash = emailHash;
            this.isChanged = isChanged;
        }
    }
}