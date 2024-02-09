package com.example.myapplication.ui.gallery;

public class AudioFile {
    private String fileName;
    private String fileId;

    public AudioFile(String fileName, String fileId) {
        this.fileName = fileName;
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileId() {
        return fileId;
    }
}
