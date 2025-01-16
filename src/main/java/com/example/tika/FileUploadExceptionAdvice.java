package com.example.tika;

import com.example.tika.DTO.FileInfoDTO;
import com.example.tika.DTO.FileResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class FileUploadExceptionAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<FileResponseDTO> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        List<FileInfoDTO> fileInfoList = new ArrayList<>();
        fileInfoList.add(new FileInfoDTO("Fail", "Fail", "File size exceeds the maximum limit (100MB). Please upload a smaller file."));

        return ResponseEntity.ok(new FileResponseDTO(1, fileInfoList));
    }
}
