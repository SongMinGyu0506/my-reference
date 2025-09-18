package com.example.filecontrol.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 해당 FileApiDto는 임시 레퍼런스용 예제임
 * 실제 프로젝트에 맞춰서 DTO 내용을 변경 할 것
 * Getter/Setter는 의도적으로 lombok을 사용하지 않았음
 * FileHandleUtil을 에러없이 사용하려면, getter/setter 메소드는 변경하지 말 것
 */
public class FileApiDto {
    /** 파일 그룹 ID */
    private Long fileGrpId;
    /** 파일 SEQ */
    private Integer fileSeq;
    /** 다중 파일 SEQ */
    private Integer[] fileSeqs;
    /** 인코딩된 파일 코드 */
    private String fileCode;

    /** 파일그룹: 파일구분 */
    private String fileDiv;
    /** 파일그룹: 게시글 ID */
    private String boardId;

    /** 파일그룹: 전체 개수 */
    private Integer fileCnt;
    /** 파일그룹: 전체 사이즈 */
    private Long fileAllSize;
    /** 개별파일: 파일 크기 */
    private Long fileSize;
    /** 개별파일: 파일 저장 경로 */
    private String filePath;
    /** 개별파일: 원본 파일명 */
    private String orignlFileNm;
    /** 개별파일: 변경 파일명 */
    private String chgFileNm;
    /** 개별파일: 파일 확장자 */
    private String fileExt;
    /** 개별파일: 파일 설명 */
    private String fileDc;

    /** 서버 내부 데이터 가져오기 여부 */
    private Boolean isInternal;

    /** 작성자ID */
    private String crtId;
    /** 작성일자 */
    private Date crtDate;
    /** 수정자ID */
    private String mdfyId;
    /** 수정일자 */
    private Date mdfyDate;



    public Long getFileGrpId() {
        return fileGrpId;
    }

    public void setFileGrpId(Long fileGrpId) {
        this.fileGrpId = fileGrpId;
    }

    public Integer getFileSeq() {
        return fileSeq;
    }

    public void setFileSeq(Integer fileSeq) {
        this.fileSeq = fileSeq;
    }

    public Integer[] getFileSeqs() {
        return fileSeqs;
    }

    public void setFileSeqs(Integer[] fileSeqs) {
        this.fileSeqs = fileSeqs;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }

    public String getFileDiv() {
        return fileDiv;
    }

    public void setFileDiv(String fileDiv) {
        this.fileDiv = fileDiv;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public Integer getFileCnt() {
        return fileCnt;
    }

    public void setFileCnt(Integer fileCnt) {
        this.fileCnt = fileCnt;
    }

    public Long getFileAllSize() {
        return fileAllSize;
    }

    public void setFileAllSize(Long fileAllSize) {
        this.fileAllSize = fileAllSize;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getOrignlFileNm() {
        return orignlFileNm;
    }

    public void setOrignlFileNm(String orignlFileNm) {
        this.orignlFileNm = orignlFileNm;
    }

    public String getChgFileNm() {
        return chgFileNm;
    }

    public void setChgFileNm(String chgFileNm) {
        this.chgFileNm = chgFileNm;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public String getFileDc() {
        return fileDc;
    }

    public void setFileDc(String fileDc) {
        this.fileDc = fileDc;
    }

    public Boolean getInternal() {
        return isInternal;
    }

    public void setInternal(Boolean internal) {
        isInternal = internal;
    }

    public String getCrtId() {
        return crtId;
    }

    public void setCrtId(String crtId) {
        this.crtId = crtId;
    }

    public Date getCrtDate() {
        return crtDate;
    }

    public void setCrtDate(Date crtDate) {
        this.crtDate = crtDate;
    }

    public String getMdfyId() {
        return mdfyId;
    }

    public void setMdfyId(String mdfyId) {
        this.mdfyId = mdfyId;
    }

    public Date getMdfyDate() {
        return mdfyDate;
    }

    public void setMdfyDate(Date mdfyDate) {
        this.mdfyDate = mdfyDate;
    }
}
