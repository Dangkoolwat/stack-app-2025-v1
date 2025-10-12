package com.daangcool.stack.service.dto;

import com.daangcool.stack.domain.board.Upload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * A DTO for the {@link com.daangcool.stack.domain.board.Upload} entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadDTO implements Serializable {

    private Long id;
    private String sourceFilename;
    private String filePath;
    private Long fileSize;
    private String fileExtension;
    private String mimeType;

    public UploadDTO(Upload upload) {
        this.id = upload.getId();
        this.sourceFilename = upload.getSourceFilename();
        this.filePath = upload.getFilePath();
        this.fileSize = upload.getFileSize();
        this.fileExtension = upload.getFileExtension();
        this.mimeType = upload.getMimeType();
    }
}
