package com.example.tika;

import com.example.tika.DTO.FileExtensionResponse;
import com.example.tika.DTO.FileInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class TikaService {
    private static final Tika tika = new Tika();
    private static final MimeTypes mimeTypes = MimeTypes.getDefaultMimeTypes();

    public FileInfoDTO extractTextAPI(MultipartFile file) {
        String extractedText;
        StringBuilder metadataOutput = new StringBuilder();
        File tempFile = null;

        try {
            tempFile = File.createTempFile("upload", file.getOriginalFilename());
            file.transferTo(tempFile);

            extractedText = extractText(tempFile, metadataOutput);

            return new FileInfoDTO(file.getOriginalFilename(), file.getContentType(), extractedText);
        } catch (IOException e) {
            log.error("Failed to process file: {}", e.getMessage(), e);
            return new FileInfoDTO(file.getOriginalFilename(), file.getContentType(), "Fail");
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    public FileExtensionResponse extractExtention(MultipartFile file) {
        File tempFile = null;

        try {
            tempFile = File.createTempFile("upload", file.getOriginalFilename());
            file.transferTo(tempFile);

            // MIME 타입 검출
            String detectedMimeType = tika.detect(tempFile);
            String expectedExtension = getExtensionFromMimeType(detectedMimeType);
            String actualExtension = getFileExtension(file.getOriginalFilename());

            if (!expectedExtension.equals(actualExtension)) {
                log.warn("File extension mismatch detected! File: {} (Expected: {}, Detected: {})",
                        file.getOriginalFilename(), expectedExtension, detectedMimeType);
                return new FileExtensionResponse(file.getOriginalFilename(), actualExtension, expectedExtension, "false");
            }

            return new FileExtensionResponse(file.getOriginalFilename(), actualExtension, expectedExtension, "true");
        } catch (IOException e) {
            log.error("Failed to process file: {}", e.getMessage(), e);
            return new FileExtensionResponse("fail", "fail", "fail", "fail");
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    private String getExtensionFromMimeType(String mimeType) {
        try {
            MimeType type = mimeTypes.forName(mimeType);
            return type.getExtension().replace(".", "");
        } catch (MimeTypeException e) {
            log.warn("Unknown MIME type: {}", mimeType);
            return "";
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }


    /**
     * Web UI용 메서드
     * @param file: 사용자로부터 전달되는 파일(단일)
     * @param metadataOutput: 사용자에게 전달되는 파일의 메타데이터
     * @return
     */
    public String extractText(File file, StringBuilder metadataOutput) {
        try {
            BodyContentHandler handler = new BodyContentHandler(-1);
            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();

            TesseractOCRConfig config = new TesseractOCRConfig();
            config.setMinFileSizeToOcr(10 * 1024);
            config.setMaxFileSizeToOcr(10 * 1024 * 1024);
            config.setLanguage("eng+kor");

            ParseContext context = new ParseContext();
            context.set(TesseractOCRConfig.class, config);

            try (InputStream stream = new FileInputStream(file)) {
                parser.parse(stream, handler, metadata, context);
            }

            for (String name : metadata.names()) {
                metadataOutput.append(name).append(": ").append(metadata.get(name)).append("\n");
            }

            return handler.toString();
        } catch (IOException | TikaException | SAXException e) {
            e.printStackTrace();
            return "Failed to extract text: " + e.getMessage();
        }
    }
}
