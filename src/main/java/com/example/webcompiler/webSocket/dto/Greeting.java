package com.example.webcompiler.webSocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Greeting {

    private String content;

    public Greeting() {
    }

    public Greeting(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}
