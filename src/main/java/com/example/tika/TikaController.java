package com.example.tika;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Controller
public class TikaController {
    @Autowired
    private TikaService tikaService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload.");
            return "index";
        }

        long maxFileSize = 50 * 1024 * 1024;
        if (file.getSize() > maxFileSize) {
            model.addAttribute("message", "File size exceeds the maximum limit (50MB). Please upload a smaller file.");
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

            // System.out.println("Filename: " + file.getOriginalFilename());
            // System.out.println("Extracted Text: " + extractedText);
            // System.out.println("Metadata: " + metadataOutput);

            tempFile.delete();
        } catch (Exception e) {
            model.addAttribute("message", "Failed to process file: " + e.getMessage());
        }

        return "index";
    }
}
