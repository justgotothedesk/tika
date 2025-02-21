package com.example.tika.DTO;

public record FileExtensionResponse(
        String fileName,
        String originExtension,
        String parsedExtension,
        String isSame
) {
}
