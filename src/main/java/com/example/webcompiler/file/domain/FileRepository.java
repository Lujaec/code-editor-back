package com.example.webcompiler.file.domain;

import com.example.webcompiler.directory.domain.Directory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    public Optional<File> findByFileUUID(final String fileUUID);

    public List<File> findAllByDirectory(final Directory directory);
}
