package com.example.tika;

import com.example.tika.DTO.FileInfoDTO;
import com.example.tika.DTO.FileResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class TikaController {
    @Autowired
    private TikaService tikaService;

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        log.info("Accessed by IP: {}", clientIp);
        return "index";
    }

    /**
     * API용 메서드
     * @param files: 사용자로부터 전달되는 여러 파일들의 정보
     * @return
     */
    @PostMapping("/api/v1/uploadFiles")
    public ResponseEntity<FileResponseDTO> requestFileAPI(@RequestParam("files") MultipartFile[] files){
        List<FileInfoDTO> fileInfoList = new ArrayList<>();

        log.info("Starting file processing");
        logSystemResources();

        try {
            for (MultipartFile file: files) {
                FileInfoDTO fileInfoDTO = tikaService.extractTextAPI(file);
                fileInfoList.add(fileInfoDTO);
            }
        } catch(Exception e) {
            log.error(e.getMessage());
            fileInfoList.add(new FileInfoDTO("Fail", "Fail", "Fail"));
        }

        log.info("File {}'s processing completed", fileInfoList.getFirst().getFileName());
        logSystemResources();
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

    /**
     * 접속하는 사용자 IP 추출을 위한 메서드
     * @param request
     * @return
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 리소스 사용량 측정을 위한 메서드
     */
    private void logSystemResources() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();

        double cpuLoad = osBean.getSystemLoadAverage(); // CPU 부하
        long freeMemory = runtime.freeMemory(); // 사용 가능한 메모리
        long totalMemory = runtime.totalMemory(); // JVM에 할당된 총 메모리
        long maxMemory = runtime.maxMemory(); // JVM이 사용할 수 있는 최대 메모리

        log.info("File: {} System Resources - CPU Load: {:.2f}, Free Memory: {} MB, Total Memory: {} MB, Max Memory: {} MB",
                cpuLoad,
                freeMemory / (1024 * 1024),
                totalMemory / (1024 * 1024),
                maxMemory / (1024 * 1024));
    }
}
