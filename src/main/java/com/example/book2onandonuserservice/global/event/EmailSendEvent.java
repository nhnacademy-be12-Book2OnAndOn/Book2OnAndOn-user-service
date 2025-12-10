package com.example.book2onandonuserservice.global.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailSendEvent {
    private String to; //수신자 메일
    private String subject;
    private String text;
}
