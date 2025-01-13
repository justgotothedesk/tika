package com.example.tika;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@Controller
public class TikaController {
    @Autowired
    private TikaService tikaService;

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        log.info("Accessed by IP: {}", clientIp);
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(MultipartFile file, Model model, HttpServletRequest request) {
        String clientIp = getClientIp(request);

        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload.");
            log.warn("IP: {} tried to upload an empty file.", clientIp);
            return "index";
        }

        long maxFileSize = 50 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            model.addAttribute("message", "File size exceeds the maximum limit (50MB). Please upload a smaller file.");
            log.warn("IP: {} attempted to upload a file exceeding the size limit ({} MB). File size: {} bytes.",
                    clientIp, maxFileSize / (1024 * 1024), file.getSize());
            return "index";
        }

        try {
            File tempFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
            file.transferTo(tempFile);

            StringBuilder metadataOutput = new StringBuilder();
            String extractedText = tikaService.extractText(tempFile, metadataOutput);

            model.addAttribute("fileName", file.getOriginalFilename());
            model.addAttribute("extractedText", extractedText);
            model.addAttribute("metadata", metadataOutput.toString());

            log.info("IP: {} uploaded file: {} (size: {} bytes). Metadata: {}",
                    clientIp, file.getOriginalFilename(), file.getSize(), metadataOutput);

            // System.out.println("Filename: " + file.getOriginalFilename());
            // System.out.println("Extracted Text: " + extractedText);
            // System.out.println("Metadata: " + metadataOutput);

            tempFile.delete();
        } catch (Exception e) {
            model.addAttribute("message", "Failed to process file: " + e.getMessage());
            log.error("IP: {} failed to process file upload. Error: {}", clientIp, e.getMessage());
        }

        return "index";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
