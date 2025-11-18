package com.example.book2onandonuserservice.user.domain.dto.request;

import com.example.book2onandonuserservice.user.domain.entity.UserGrade;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UserCreateDTO {
    String password;
    String userLoginId;
    String name;
    String email;
    String phone;
    LocalDate birth;
    String providerName;
    String providerId;
    UserGrade userGrade;
}
