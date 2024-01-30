package com.example.webcompiler.directory.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DirectoryRepository extends JpaRepository<Directory, Long> {
    Optional<Directory> findByDirectoryUUID(final String directoryUUID);

    List<Directory> findAllByUserId(final Long userId);
}
