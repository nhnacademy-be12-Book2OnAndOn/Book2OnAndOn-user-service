package com.example.book2onandonuserservice.auth.repository;

import com.example.book2onandonuserservice.auth.domain.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
