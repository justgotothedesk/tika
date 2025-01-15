package com.example.tika.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoDTO {
    private String filePath;
    private String contentType;
    private String textData;
}
