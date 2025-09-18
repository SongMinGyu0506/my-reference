package com.example.filecontrol.file.service.impl;

import com.example.filecontrol.file.dto.FileApiDto;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileHandleUtil {

    private static final String ZIP_TEMP_DIR = "실제 프로젝트에서 사용할 파일 저장 임시경로";

    /**
     * 파일 - 화면에 바로 출력 (이미지, PDF...)
     * @param request HTTP Request Object
     * @param response HTTP Response Object
     * @param file Target Download File Metadata
     * @throws FileNotFoundException File not found
     * @throws IOException File Process Exception
     */
    public static void showFile(HttpServletRequest request, HttpServletResponse response, FileApiDto file) throws FileNotFoundException, IOException {
        File path = new File(file.getFilePath(), file.getChgFileNm() + "." + file.getFileExt());
        if(!path.exists() || !path.isFile()) { throw new FileNotFoundException("Developer Make Exception:: CANNOT found file");}

        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));) {
            ServletOutputStream sos = response.getOutputStream();
            setResponseHeader(file.getOrignlFileNm()+"."+file.getFileExt(), path.length(), false, request, response);
            response.setContentType(getMimeType(file.getFileExt()));
            FileCopyUtils.copy(bis,sos);
        }
    }

    /**
     * 파일 - 다운로드
     * @param request HTTP Request Object
     * @param response HTTP Response Object
     * @param file Target Download File Metadata
     * @throws FileNotFoundException File not found
     * @throws IOException File Process Exception
     */
    public static void downFile(HttpServletRequest request, HttpServletResponse response, FileApiDto file) throws FileNotFoundException, IOException {
        File path = new File(file.getFilePath(), file.getChgFileNm() + "."+file.getFileExt());
        if(!path.exists() || !path.isFile()) { throw new FileNotFoundException("Developer Make Exception:: CANNOT found file");}

        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path))) {
            ServletOutputStream sos = response.getOutputStream();
            setResponseHeader(file.getOrignlFileNm()+"."+file.getFileExt(),path.length(), true, request,response);
        }
    }
    public static void downZipFile(HttpServletRequest request, HttpServletResponse response,
                                   String fileName, List<FileApiDto> files) throws IOException {
        // 1) 임시 디렉토리 준비
        try {
            File tempDir = new File(ZIP_TEMP_DIR);
            if (!tempDir.exists()) {
                tempDir.setReadable(true, true);
                tempDir.setWritable(true, true);
                tempDir.setExecutable(true, true);
                boolean mkdirResult = tempDir.mkdir();
                if (!mkdirResult) {
                    throw new IOException("Developer Make Exception::Cannot Create Temporary Directory");
                }
            }
        } catch (SecurityException e) {
            throw new IOException("Developer Make Exception::Violate Security Policies", e);
        } catch (IOException e) {
            throw new IOException("Developer Make Exception::Error while create save directory", e);
        }

        // 2) ZIP 파일 생성
        File zipFile = new File(ZIP_TEMP_DIR, getTimestamp() + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
            byte[] buffer = new byte[8192];
            Set<String> usedNames = new HashSet<>(); // ZIP 내부 파일명 중복 방지

            for (FileApiDto file : files) {
                String originalFileNm = file.getOrignlFileNm() + "." + file.getFileExt();
                String chgFileNm = file.getChgFileNm() + "." + file.getFileExt();

                File itemPath = new File(file.getFilePath(), chgFileNm);
                if (!itemPath.exists() || !itemPath.isFile()) {
                    continue;
                }

                // 엔트리명 안전화 + 중복 처리
                String entryName = dedupeEntryName(safeEntryName(originalFileNm), usedNames);

                zos.putNextEntry(new ZipEntry(entryName));
                try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(itemPath))) {
                    int n;
                    while ((n = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, n);
                    }
                } finally {
                    zos.closeEntry();
                }
            }
        }

        // 3) 파일 다운로드
        if (!zipFile.exists() || !zipFile.isFile()) {
            throw new FileNotFoundException("NOT created file");
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipFile));
             ServletOutputStream sos = response.getOutputStream()) {
            // 네가 쓰는 공통 헤더 유틸 그대로 유지
            setResponseHeader(fileName + ".zip", zipFile.length(), true, request, response);
            FileCopyUtils.copy(bis, sos);
        } finally {
            // 임시 ZIP 정리 (복구 불가한 예외는 무시)
            try { Files.deleteIfExists(zipFile.toPath()); } catch (IOException ignore) {}
        }
    }

    public static List<FileApiDto> parseMultipartAndSaveFiles(List<MultipartFile> files, String chgFileNmPrefix, String dirPath) throws IOException {
        createDir(dirPath);

        List<FileApiDto> returnObj = new ArrayList<>();
        String timestampStr = getTimestamp();
        for(int idx = 0; idx < files.size(); idx++) {
            MultipartFile fileObj = files.get(idx);
            if(fileObj == null) { continue;}
            String chgFileNm = chgFileNmPrefix + timestampStr + String.format("%04d",idx);
            String orgFileNm = fileObj.getOriginalFilename();
            if(orgFileNm == null || orgFileNm.equals(" ")) { orgFileNm=chgFileNm; }
            String fileExt = orgFileNm.substring(orgFileNm.lastIndexOf(".")+1);
            if(fileExt!=null) {orgFileNm = orgFileNm.substring(0,orgFileNm.length()-(fileExt.length()+1));}
            long fileSize = fileObj.getSize();

            try {
                File path = new File(dirPath, chgFileNm+"."+fileExt);
                fileObj.transferTo(path);
            } catch (IOException e) {
                throw new IOException("Developer Make Exception::Error while save files",e);
            }

            FileApiDto file = new FileApiDto();
            file.setFileSize(fileSize);
            file.setFilePath(dirPath);
            file.setOrignlFileNm(orgFileNm);
            file.setChgFileNm(chgFileNm);
            file.setFileExt(fileExt);
            returnObj.add(file);
        }
        return returnObj;
    }

    public static void deleteFiles(List<FileApiDto> files) throws IOException {
        for(FileApiDto file : files) {
            File path = new File(file.getFilePath(), file.getChgFileNm() + "." + file.getFileExt());
            if(path.exists() && path.isFile()) path.delete();
        }
    }

    /**
     * 파일을 저장할 디렉토리를 생성한다.
     * @param path 디렉토리 경로
     * @throws IOException 생성 중 오류
     */
    private static void createDir(String path) throws IOException {
        File pathObj = new File(path);
        createDir(pathObj);
    }

    /**
     * 파일을 저장할 디렉토리를 생성한다.
     * @param path 디렉토리 경로
     * @throws IOException 생성 중 오류
     */
    private static void createDir(File path) throws IOException {
        try {
            if(!path.exists()) {
                // 저장 경로가 없을 경우 생성, 권한 555
                path.setReadable(true, false);
                path.setWritable(true, false);
                path.setExecutable(true, false);
                boolean result = path.mkdirs();
                if(!result) {
                    throw new IOException("CANNOT CREATE save directory");
                }
            }
        } catch(SecurityException e) {
            throw new IOException("ERROR because violate security policies", e);
        } catch(IOException e) {
            throw new IOException("ERROR while create save directory", e);
        }
    }

    /** ZIP 엔트리명 안전화: 경로/제어문자 제거, 빈값 대비 */
    private static String safeEntryName(String name) {
        if (name == null || name.trim().isEmpty()) return "file";
        String base = name.trim().replace("\\", "/");
        int idx = base.lastIndexOf('/');
        if (idx >= 0) base = base.substring(idx + 1);
        base = base.replaceAll("[\\r\\n\\t]", "");
        return base.isEmpty() ? "file" : base;
    }

    /** 중복 엔트리명 처리: file.ext → file (1).ext, file (2).ext ... */
    private static String dedupeEntryName(String name, Set<String> used) {
        if (used.add(name)) return name;
        int dot = name.lastIndexOf('.');
        String base = (dot >= 0) ? name.substring(0, dot) : name;
        String ext  = (dot >= 0) ? name.substring(dot) : "";
        int i = 1;
        String candidate;
        do {
            candidate = base + " (" + (i++) + ")" + ext;
        } while (!used.add(candidate));
        return candidate;
    }



    /**
     * 파일명을 생성하기 위해 현재 시점 timestamp String을 생성한다.
     * @return timestamp String
     */
    private static String getTimestamp() {
        String rtnStr = null;
        String pattern = "yyyyMMddhhmmssSSS";
        SimpleDateFormat sdfCurrent = new SimpleDateFormat(pattern, Locale.KOREA);
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        rtnStr = sdfCurrent.format(ts.getTime());
        return rtnStr;
    }

    /**
     * 파일 확장자에 따른 MIME Type을 가져온다
     * @param fileExt 대상 파일 확장자
     * @return MIME Type
     * @throws IOException 처리 중 오류
     */
    private static String getMimeType(String fileExt) throws IOException {
        return Files.probeContentType(Paths.get("tmp." + fileExt));
    }


    private static void setResponseHeader(String filename, long filesize, boolean isAttachment, HttpServletRequest request, HttpServletResponse response)
            throws UnsupportedEncodingException {
        String browser = getBrowser(request);

        String dispositionPrefix = (isAttachment ? "attachment" : "inline") + "; filename=";
        String encodedFilename = null;

        if (browser.equals("MSIE")) {
            encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        } else if (browser.equals("Trident")) {
            encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        } else if (browser.equals("Firefox")) {
            encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
        } else if (browser.equals("Opera")) {
            encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
        } else if (browser.equals("Chrome")) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < filename.length(); i++) {
                char c = filename.charAt(i);
                if (c > '~') {
                    sb.append(URLEncoder.encode("" + c, "UTF-8"));
                } else {
                    sb.append(c);
                }
            }
            encodedFilename = sb.toString();
        } else {
            throw new UnsupportedEncodingException("Not supported browser");
        }

//		response.setHeader("Content-Disposition", dispositionPrefix + encodedFilename);
        response.setHeader("Content-Disposition", dispositionPrefix + "\"" +encodedFilename + "\"");
        response.setContentLength((int) filesize);
        response.setContentType("application/octet-stream;charset=UTF-8");
    }


    /**
     * 브라우저 구분값 반환
     * @param request HTTP request 객체
     * @return 브라우저 종료
     */
    private static String getBrowser(HttpServletRequest request) {
        String header = request.getHeader("User-Agent");
        if (header.indexOf("MSIE") > -1) {
            return "MSIE";
        } else if (header.indexOf("Trident") > -1) { // IE11
            return "Trident";
        } else if (header.indexOf("Chrome") > -1) {
            return "Chrome";
        } else if (header.indexOf("Opera") > -1) {
            return "Opera";
        }
        return "Firefox";
    }






    @Deprecated
    public static void downZipFile_deprecated(HttpServletRequest request, HttpServletResponse response, String fileName, List<FileApiDto> files) throws IOException {
        try {
            File path = new File(ZIP_TEMP_DIR);
            if(!path.exists()) {
                path.setReadable(true,true);
                path.setWritable(true,true);
                path.setExecutable(true, true);
                boolean mkdirResult = path.mkdir();
                if(!mkdirResult) throw new IOException("Developer Make Exception::Cannot Create Temporary Directory");
            }
        } catch (SecurityException e) {
            throw new IOException("Developer Make Exception::Violate Security Policies",e);
        } catch (IOException e) {
            throw new IOException("Developer Make Exception::Error while create save directory");
        }

        File path = new File(ZIP_TEMP_DIR,getTimestamp()+".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path))) {
            byte[] buffer = new byte[4096];
            for (FileApiDto file : files) {
                String originalFileNm = file.getOrignlFileNm()+"."+file.getFileExt();
                String chgFileNm = file.getChgFileNm()+"."+file.getFileExt();
                File itemPath = new File(file.getFilePath(),chgFileNm);
                if(!itemPath.exists() || !itemPath.isFile()) {continue;}
                try(FileInputStream fis = new FileInputStream(itemPath)) {
                    zos.putNextEntry(new ZipEntry(originalFileNm));
                    int itemLength = 0;
                    while((itemLength = fis.read(buffer)) > 0) {
                        zos.write(buffer,0,itemLength);
                    }
                } finally {
                    zos.closeEntry();
                }
            }
        }

        // 파일 다운로드
        if(!path.exists() || !path.isFile()) { throw new FileNotFoundException("NOT created file"); }

        try( BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path)) ) {
            ServletOutputStream sos = response.getOutputStream();
            setResponseHeader(fileName + ".zip", path.length(), true, request, response);
            FileCopyUtils.copy(bis, sos);
            Files.deleteIfExists(path.toPath());
            sos.close();
        }
    }
}
