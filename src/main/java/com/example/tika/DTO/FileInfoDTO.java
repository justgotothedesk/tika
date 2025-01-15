package com.example.tika.DTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoDTO {
    private String fileName;
    private String contentType;
    private String textData;
}
