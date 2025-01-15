package com.example.tika.DTO;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileResponseDTO {
    private int fileCount;
    private List<FileInfoDTO> infos;
}
