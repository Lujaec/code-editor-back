package com.example.webcompiler.file.domain;

import com.example.webcompiler.directory.domain.Directory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    private String title;
    private String content;
    @Enumerated(EnumType.STRING)
    private Extension extension;

    private String fileUUID;

    @ManyToOne
    @JoinColumn(name = "Directory_id")
    Directory directory;
}
