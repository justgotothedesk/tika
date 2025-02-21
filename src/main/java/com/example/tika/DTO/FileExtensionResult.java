package com.example.tika.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileExtensionResult {
    private String fileName;
    private String originExtension;
    private String parsedExtension;
    private String isSame;
}
