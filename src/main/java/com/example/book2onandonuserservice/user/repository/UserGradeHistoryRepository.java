package com.example.book2onandonuserservice.user.repository;

import com.example.book2onandonuserservice.user.domain.entity.UserGradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGradeHistoryRepository extends JpaRepository<UserGradeHistory, Long> {
}