package com.example.filecontrol.file.service.impl;

import com.example.filecontrol.file.dto.FileApiDto;
import com.example.filecontrol.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String CMM_FILE_DIR = "실제 프로젝트에서 사용하는 파일 디렉토리 경로";
    private static final List<String> CMM_UPLOADABLE_EXT = Arrays.asList(new String[]{"실제 프로젝트에서 허용하는 파일 확장자"});

    private final FileMetadataService metadataService;


    @Override
    public Long saveFile(List<MultipartFile> files, Long existFileGrpId, String user) throws IOException {
        //검증
        for(MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            String fileExt = "";
            if(filename != null && !filename.equals(" ")) {
                fileExt = filename.substring(filename.lastIndexOf("."));
                if(!fileExt.isEmpty()) { fileExt = fileExt.substring(1).toLowerCase();}
            }
            if(!CMM_UPLOADABLE_EXT.contains(fileExt)) {
                throw new IOException("Developer Make Exception::Not Allowed File Exception");
            }
        }

        List<FileApiDto> savedFiles = FileHandleUtil.parseMultipartAndSaveFiles(files, "FILE_", CMM_FILE_DIR);
        if(existFileGrpId != null) {
            for(FileApiDto file : savedFiles) {
                file.setFileGrpId(existFileGrpId);
            }
        }

        FileApiDto fileApiDto = new FileApiDto();
        if(existFileGrpId == null) {
            //필요한 dto 정보 set으로 추가
            metadataService.fileGrpCreate(); // SUDO 코드
        }

        metadataService.fileDtlCreate(); // SUDO 코드
        return fileApiDto.getFileGrpId();
    }

    @Override
    public Long saveFile(List<MultipartFile> files, String user) throws IOException {
        return this.saveFile(files,null,user);
    }

    @Override
    public boolean deleteFile(long fileGrpId, Integer fileInfoSeq) throws IOException {
        int result = 0;
        metadataService.fileDtlRead();
        List<FileApiDto> temp = new ArrayList<>();
        FileHandleUtil.deleteFiles(temp);

        metadataService.fileDtlDelete(); // sudo code
        if(fileInfoSeq == null) {
            metadataService.fileGrpDelete(); //sudo code
        }
        return result > 0;
    }

    @Override
    public boolean deleteFile(long fileGrpId) throws IOException {
        return this.deleteFile(fileGrpId,null);
    }
}
