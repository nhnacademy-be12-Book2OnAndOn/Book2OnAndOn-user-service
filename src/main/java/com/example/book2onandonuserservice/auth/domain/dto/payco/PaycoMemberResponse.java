package com.example.book2onandonuserservice.auth.domain.dto.payco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaycoMemberResponse {
    private PaycoHeader header;
    private PaycoData data;

    @Getter
    @NoArgsConstructor
    public static class PaycoHeader {
        @JsonProperty("isSuccessful")
        private boolean isSuccessful;
        private int resultCode;
        private String resultMessage;
    }

    @Getter
    @NoArgsConstructor
    public static class PaycoData {
        private PaycoMember member;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaycoMember {
        @JsonProperty("idNo")
        private String idNo;

        private String email;
        private String name;
        private String mobile;

        @JsonProperty("birthdayMMdd") //페이코에서 제공하는 JSON key
        private String birthday;
    }
}