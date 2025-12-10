package com.example.book2onandonuserservice.point.support.pointHistory;

import com.example.book2onandonuserservice.user.domain.entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class UserReferenceLoader {

    @PersistenceContext
    private EntityManager entityManager;

    // 1. Users 엔티티 프록시 로딩 (SELECT 없이 참조만)
    public Users getReference(Long userId) {
        return entityManager.getReference(Users.class, userId);
    }

}
//    1) new Users(userId)
//    → 이렇게 만들면 JPA가 “진짜 Users인지” 모름 → 비정상 엔티티
//    2) SELECT로 Users를 가져온 뒤 setter
//    → SELECT 쿼리가 매번 발생함 (성능 낭비)

