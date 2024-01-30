package com.example.webcompiler.directory.application;


import com.example.webcompiler.directory.application.dto.DirectoryCreateDto;
import com.example.webcompiler.directory.application.dto.DirectoryDeleteDto;
import com.example.webcompiler.directory.application.dto.DirectoryUpdateDto;
import com.example.webcompiler.directory.domain.Directory;
import com.example.webcompiler.directory.domain.DirectoryRepository;
import com.example.webcompiler.directory.presentation.dto.response.DirectoryInfoResponse;
import com.example.webcompiler.directory.presentation.dto.response.DirectoryInfoResponses;
import com.example.webcompiler.file.domain.File;
import com.example.webcompiler.file.presentation.dto.response.FileInfoResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DirectoryService {
    private final DirectoryRepository directoryRepository;
    private final ModelMapper mapper;
    public DirectoryInfoResponse create(DirectoryCreateDto dto){
        Directory directory = mapper.map(dto, Directory.class);
        directory.setDirectoryUUID(UUID.randomUUID().toString());

        Directory save = directoryRepository.save(directory);
        return convertToDirectoryInfoResponse(save);
    }

    public DirectoryInfoResponse update(DirectoryUpdateDto dto){
        final String directoryUUID = dto.getDirectoryUUID();
        Optional<Directory> directoryWrapper = directoryRepository.findByDirectoryUUID(dto.getDirectoryUUID());

        if(directoryWrapper.isEmpty()){
            log.info("UUID = {} 인 디렉토리는 존재하지 않습니다", directoryUUID);
            throw new IllegalArgumentException();
        }

        Directory directory = directoryWrapper.get();
        final String title = dto.getTitle();
        final String explanation = dto.getExplanation();

        if(dto.getTitle() != null){
            directory.setTitle(title);
        }
        if(dto.getExplanation() != null){
            directory.setExplanation(explanation);
        }

        return convertToDirectoryInfoResponse(directory);
    }
    
    public void delete(DirectoryDeleteDto dto){
        final String directoryUUID = dto.getDirectoryUUID();

        Optional<Directory> directoryWrapper = directoryRepository.findByDirectoryUUID(dto.getDirectoryUUID());

        if(directoryWrapper.isEmpty()){
            log.info("UUID = {} 인 디렉토리는 존재하지 않습니다", directoryUUID);
            throw new IllegalArgumentException();
        }

        directoryRepository.delete(directoryWrapper.get());
    }
    
    public DirectoryInfoResponse findByUUID(String directoryUUID){
        Optional<Directory> directoryWrapper = directoryRepository.findByDirectoryUUID(directoryUUID);

        if(directoryWrapper.isEmpty()){
            log.info("UUID = {} 인 디렉토리는 존재하지 않습니다", directoryUUID);
            throw new IllegalArgumentException();
        }

        Directory directory = directoryWrapper.get();
        return convertToDirectoryInfoResponse(directory);
    }
    
    public DirectoryInfoResponses findByUserId(Long userId){
        List<Directory> directories = directoryRepository.findAllByUserId(userId);

        List<DirectoryInfoResponse> infoResponses = directories.stream().map((e) -> {
            DirectoryInfoResponse directoryInfoResponse = convertToDirectoryInfoResponse(e);
            return directoryInfoResponse;
        }).collect(Collectors.toList());

        return new DirectoryInfoResponses(infoResponses);
    }

    private DirectoryInfoResponse convertToDirectoryInfoResponse(Directory directory){
        DirectoryInfoResponse infoResponse = mapper.map(directory, DirectoryInfoResponse.class);
        List<File> files = directory.getFiles();

        infoResponse.setFilInfos(
                files.stream()
                        .map((e) -> {
                            FileInfoResponse fileInfoResponse = mapper.map(e, FileInfoResponse.class);
                            fileInfoResponse.setDirectoryUUID(directory.getDirectoryUUID());
                            return fileInfoResponse;
                        })
                        .collect(Collectors.toList())
        );

        return infoResponse;
    }
}
