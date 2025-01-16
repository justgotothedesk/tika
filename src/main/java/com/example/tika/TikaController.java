package com.example.tika;

import com.example.tika.DTO.FileInfoDTO;
import com.example.tika.DTO.FileResponseDTO;
import com.example.tika.Util.RequestUtil;
import com.example.tika.Util.SystemResourceUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class TikaController {
    @Autowired
    private TikaService tikaService;

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        String clientIp = RequestUtil.getClientIp(request);
        log.info("Accessed by IP: {}", clientIp);
        return "index";
    }

    /**
     * API용 메서드
     * @param files: 사용자로부터 전달되는 여러 파일들의 정보
     * @return
     */
    @PostMapping("/api/v1/uploadFiles")
    public ResponseEntity<FileResponseDTO> requestFileAPI(@RequestParam("files") MultipartFile[] files) {
        List<FileInfoDTO> fileInfoList = new ArrayList<>();

        if (files == null || files.length == 0) {
            log.warn("No files were uploaded.");
            return ResponseEntity.badRequest().body(new FileResponseDTO(0, fileInfoList));
        }

        for (MultipartFile file : files) {
            List<Double> beforeResource = SystemResourceUtil.getSystemResources();
            try {
                FileInfoDTO fileInfoDTO = tikaService.extractTextAPI(file);
                fileInfoList.add(fileInfoDTO);

                List<Double> afterResource = SystemResourceUtil.getSystemResources();
                double memoryChange = afterResource.get(0) - beforeResource.get(0);
                double cpuChange = (afterResource.get(1) - beforeResource.get(1)) * 100;

                log.info("File '{}' processed successfully. Used Memory: {} MB, CPU Load Change: {}%",
                        file.getOriginalFilename(), memoryChange, cpuChange);
            } catch (Exception e) {
                log.error("Failed to process file '{}'. Error: {}", file.getOriginalFilename(), e.getMessage());
                fileInfoList.add(new FileInfoDTO("Fail", "Fail", "Fail"));
            }
        }

        FileResponseDTO response = new FileResponseDTO(files.length, fileInfoList);

        return ResponseEntity.ok(response);
    }


    /**
     * Web UI용 메서드
     * @param file: 사용자로부터 전달되는 파일(단일)
     * @param model: 전달용 JSON 파일
     * @param request: 사용자 IP 로깅용
     * @return
     */
    @PostMapping("/upload")
    public String uploadFile(MultipartFile file, Model model, HttpServletRequest request) {
        String clientIp = RequestUtil.getClientIp(request);

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

            tempFile.delete();
        } catch (Exception e) {
            model.addAttribute("message", "Failed to process file: " + e.getMessage());
            log.error("IP: {} failed to process file upload. Error: {}", clientIp, e.getMessage());
        }

        return "index";
    }
}
