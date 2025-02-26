package com.example.tika.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileExtensionResponse {
    private int fileCount;
    private List<FileExtensionResult> results;
}

