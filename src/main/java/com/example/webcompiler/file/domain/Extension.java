package com.example.webcompiler.file.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Extension {
    PY("py"),
    C("c"),
    CPP("cpp");

    private String exec;

    Extension(String exec){
        this.exec = exec;
    }

    @JsonCreator
    public static Extension from(String s){
        return Extension.valueOf(s.toUpperCase());
    }
}
