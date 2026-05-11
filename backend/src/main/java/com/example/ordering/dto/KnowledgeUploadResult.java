package com.example.ordering.dto;

public class KnowledgeUploadResult {

    private final String filename;
    private final String fileType;
    private final boolean success;
    private final int chunkCount;
    private final String errorMessage;

    private KnowledgeUploadResult(String filename, String fileType, boolean success, int chunkCount, String errorMessage) {
        this.filename = filename;
        this.fileType = fileType;
        this.success = success;
        this.chunkCount = chunkCount;
        this.errorMessage = errorMessage;
    }

    public static KnowledgeUploadResult success(String filename, String fileType, int chunkCount) {
        return new KnowledgeUploadResult(filename, fileType, true, chunkCount, null);
    }

    public static KnowledgeUploadResult failure(String filename, String fileType, String errorMessage) {
        return new KnowledgeUploadResult(filename, fileType, false, 0, errorMessage);
    }

    public String getFilename() { return filename; }
    public String getFileType() { return fileType; }
    public boolean isSuccess() { return success; }
    public int getChunkCount() { return chunkCount; }
    public String getErrorMessage() { return errorMessage; }
}
