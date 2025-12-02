package com.example.book2onandonuserservice.global.client;

import com.example.book2onandonuserservice.global.dto.RestPage;
import com.example.book2onandonuserservice.point.domain.dto.response.CurrentPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.EarnPointResponseDto;
import com.example.book2onandonuserservice.point.domain.dto.response.PointHistoryResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "point-service")
public interface PointServiceClient {


    @GetMapping("/users/me/points")
    RestPage<PointHistoryResponseDto> getMyPointHistory(
            @RequestHeader("X-User-Id") Long userId,
            Pageable pageable
    );


    @GetMapping("/users/me/points/current")
    CurrentPointResponseDto getCurrentPoint(
            @RequestHeader("X-User-Id") Long userId
    );


    @PostMapping("/users/me/points/earn/signup")
    EarnPointResponseDto earnSignupPoint(
            @RequestHeader("X-User-Id") Long userId
    );
}