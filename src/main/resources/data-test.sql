-- UserGrade (테이블명 주의)
INSERT INTO user_grade (user_grade_name,
                        user_point_add_rate,
                        grade_cutline)
VALUES (0, 0.0, 0);

-- Users
INSERT INTO users (user_id,
                   user_grade_id,
                   user_nickname,
                   user_login_id,
                   user_password,
                   user_email,
                   user_name,
                   user_phone,
                   user_role,
                   user_status,
                   user_created_at,
                   user_latest_login)
VALUES (1,
        1,
        'tester',
        'testuser',
        '$2a$10$abcdefghijklmnopqrstuv',
        'test@example.com',
        '테스트유저',
        '01012345678',
        'USER',
        'ACTIVE',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);

-- Point Policy
INSERT INTO point_policy (point_policy_name,
                          point_add_rate,
                          point_add_point,
                          point_is_active)
VALUES ('SIGNUP', null, 5000, true),
       ('ORDER', 0.01, null, true),
       ('REVIEW', null, 200, true),
       ('REVIEW_PHOTO', null, 500, true);
