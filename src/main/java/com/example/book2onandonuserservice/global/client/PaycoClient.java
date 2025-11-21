package com.example.book2onandonuserservice.global.client;

import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoMemberResponse;
import com.example.book2onandonuserservice.auth.domain.dto.payco.PaycoTokenResponse;
import java.net.URI;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

//페이코와 통신하기 위한 인터페이스
@FeignClient(name = "payco-client", url = "https://id.payco.com")
public interface PaycoClient {
    //Access Token 발급요청
    @PostMapping("/oauth2.0/token")
    PaycoTokenResponse getToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code
    );

    //회원 정보 조회 요청
    @PostMapping
    PaycoMemberResponse getMemberInfo(
            URI baseUrl,
            @RequestHeader("client_id") String clientId,
            @RequestHeader("access_Token") String accessToken
    );

}
