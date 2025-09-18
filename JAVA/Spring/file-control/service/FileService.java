package com.example.filecontrol.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {
    Long saveFile(List<MultipartFile> files, Long existFileGrpId, String user) throws IOException;
    Long saveFile(List<MultipartFile> files, String user) throws IOException;
    boolean deleteFile(long fileGrpId, Integer fileInfoSeq) throws IOException;
    boolean deleteFile(long fileGrpId) throws IOException;
}
