package com.example.book2onandonuserservice.auth.domain.dto.payco;

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
    public static class PaycoMember {
        @JsonProperty("idNo")
        private String idNo;

        private String email;
        private String name;
        private String mobile;
        private String birthday;
    }
}
