package com.example.webcompiler.directory.domain;

import com.example.webcompiler.file.domain.File;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Directory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String title;
    private String explanation;

    private String directoryUUID;
    private Long userId;
    @OneToMany(mappedBy = "directory")
    private List<File> files = new ArrayList<>();
}
