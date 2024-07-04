package com.example.webcompiler.file.application;

import com.example.webcompiler.directory.domain.Directory;
import com.example.webcompiler.directory.domain.DirectoryRepository;
import com.example.webcompiler.file.application.dto.FileCreateDto;
import com.example.webcompiler.file.application.dto.FileDeleteDto;
import com.example.webcompiler.file.application.dto.FileExecuteDto;
import com.example.webcompiler.file.application.dto.FileUpdateDto;
import com.example.webcompiler.file.domain.Extension;
import com.example.webcompiler.file.domain.File;
import com.example.webcompiler.file.domain.FileRepository;
import com.example.webcompiler.file.presentation.dto.response.FileInfoResponse;
import com.example.webcompiler.file.presentation.dto.response.FileInfoResponses;
import com.example.webcompiler.ssh.application.SshService;
import com.example.webcompiler.ssh.domain.SshConnection;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final FileRepository fileRepository;
    private final DirectoryRepository directoryRepository;
    private final ModelMapper mapper;
    private final SshService sshService;

    @Transactional
    public FileInfoResponse create(FileCreateDto dto){
        File file = mapper.map(dto, File.class);
        Optional<Directory> directoryWrapper = directoryRepository.findByDirectoryUUID(dto.getDirectoryUUID());

        if(directoryWrapper.isEmpty()){
            log.info("directoryUUID = {}인 디렉토리는 존재하지 않습니다", dto.getDirectoryUUID());
            throw new IllegalArgumentException();
        }

        Directory directory = directoryWrapper.get();
        file.setDirectory(directory);
        file.setFileUUID(UUID.randomUUID().toString());

        fileRepository.save(file);
        return convertToFileInfoResponse(file);
    }

    @Transactional
    public FileInfoResponse update(FileUpdateDto dto){
        Optional<File> fileWrapper = fileRepository.findByFileUUID(dto.getFileUUID());

        if(fileWrapper.isEmpty()){
            log.info("fileUUID = {}인 파일은 존재하지 않습니다", dto.getFileUUID());
            throw new IllegalArgumentException();
        }

        File file = fileWrapper.get();
        final String title = dto.getTitle();
        final String content = dto.getContent();
        final Extension extension = dto.getExtension();

        if(title != null)
            file.setTitle(title);
        if(content != null)
            file.setContent(content);
        if(extension != null)
            file.setExtension(extension);

        return convertToFileInfoResponse(file);
    }

    @Transactional
    public void delete(FileDeleteDto dto){
        Optional<File> fileWrapper = fileRepository.findByFileUUID(dto.getFileUUID());

        if(fileWrapper.isEmpty()){
            log.info("fileUUID = {}인 파일은 존재하지 않습니다", dto.getFileUUID());
            throw new IllegalArgumentException();
        }

        fileRepository.delete(fileWrapper.get());
    }

    public FileInfoResponse findByUUID(String fileUUID){
        Optional<File> fileWrapper = fileRepository.findByFileUUID(fileUUID);

        if(fileWrapper.isEmpty()){
            log.info("fileUUID = {}인 파일은 존재하지 않습니다", fileUUID);
            throw new IllegalArgumentException();
        }

        return convertToFileInfoResponse(fileWrapper.get());
    }

    public FileInfoResponses findAllByDirectoryUUID(String directoryUUID){
        Optional<Directory> directoryWrapper = directoryRepository.findByDirectoryUUID(directoryUUID);

        if(directoryWrapper.isEmpty()){
            log.info("directoryUUID = {}인 디렉토리는 존재하지 않습니다", directoryUUID);
            throw new IllegalArgumentException();
        }


        List<File> files = fileRepository.findAllByDirectory(directoryWrapper.get());

        List<FileInfoResponse> fileInfoResponseList = files.stream()
                .map((e) -> {
                    return convertToFileInfoResponse(e);
                }).collect(Collectors.toList());


        return new FileInfoResponses(fileInfoResponseList);
    }

    private FileInfoResponse convertToFileInfoResponse(File file){
        Directory directory = file.getDirectory();

        if(directory == null){
            log.info("FileUUID = {}인 파일의 디렉토리는 존재하지 않습니다.");
            throw new IllegalArgumentException();
        }

        FileInfoResponse infoResponse = mapper.map(file, FileInfoResponse.class);
        infoResponse.setDirectoryUUID(directory.getDirectoryUUID());
        return infoResponse;
    }

    public void execute(FileExecuteDto dto) throws IOException {
        String webSocketSessionId = dto.getWebSocketSessionId();
        SshConnection sshConnection = sshService.findByWebSocketSessionId(webSocketSessionId);
        Extension extension = dto.getExtension();

        createFile(sshConnection, dto);
        sshService.transToSSh(sshConnection, "clear\n");

        if(extension == Extension.C)
            executeC(sshConnection, dto);
        else if(extension == Extension.CPP)
            executeCpp(sshConnection, dto);
        else if(extension == Extension.PY)
            executePy(sshConnection, dto);
    }

    private void createFile(SshConnection sshConnection, FileExecuteDto dto) throws IOException {
        String sourceFile = dto.getTitle();
        String content = dto.getContent();

        String replacedContent = content.replace("\"", "\\\"");
        String fileDeleteCommand = "rm " + sourceFile + "\n";
        sshService.transToSSh(sshConnection, fileDeleteCommand);

        String fileCreateCommand = "echo \"" +  replacedContent + "\"" +
                " >> " + sourceFile + "\n";

        sshService.transToSSh(sshConnection, fileCreateCommand);
    }


    private void executeC(SshConnection sshConnection, FileExecuteDto dto) throws IOException {
        String sourceFileName = dto.getTitle();
        String noExecTitle = dto.getTitle().replaceAll("\\.(.*?)$", "");
        String compileCommand = "gcc " + sourceFileName + " -o " + noExecTitle + "\n";
        String executeCommand = "./" + noExecTitle + "\n";

        sshService.transToSSh(sshConnection, compileCommand);

        sshService.transToSSh(sshConnection, executeCommand);
    }

    private void executeCpp(SshConnection sshConnection, FileExecuteDto dto) throws IOException {
        log.info("executeCpp !!");

        String sourceFileName = dto.getTitle();
        String noExecTitle = dto.getTitle().replaceAll("\\.(.*?)$", "");
        String compileCommand = "g++ " + sourceFileName + " -o " + noExecTitle + "\n";
        String executeCommand = "./" + noExecTitle + "\n";

        sshService.transToSSh(sshConnection, compileCommand);
        sshService.transToSSh(sshConnection, executeCommand);
    }

    private void executePy(SshConnection sshConnection, FileExecuteDto dto) throws IOException {
        String sourceFileName = dto.getTitle();
        String executeCommand = "python3 " + sourceFileName + "\n";

        sshService.transToSSh(sshConnection, executeCommand);
    }
}
