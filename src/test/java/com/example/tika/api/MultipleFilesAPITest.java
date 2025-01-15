package com.example.tika.api;

import com.example.tika.DTO.FileInfoDTO;
import com.example.tika.TikaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MultipleFilesAPITest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TikaService tikaService;

    @DisplayName("여러 파일을 API에 요청했을 때, 올바른 텍스트 데이터를 반환하는지 확인한다.")
    @Test
    void checkExactResponseData() throws Exception {
        when(tikaService.extractTextAPI(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> {
                    MockMultipartFile file = invocation.getArgument(0);
                    return new FileInfoDTO(file.getOriginalFilename(), file.getContentType(), new String(file.getBytes()));
                });

        MockMultipartFile file1 = new MockMultipartFile(
                "files", "test1.txt", MediaType.TEXT_PLAIN_VALUE, "This is a test file 1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "test2.txt", MediaType.TEXT_PLAIN_VALUE, "This is a test file 2".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/uploadFiles")
                        .file(file1)
                        .file(file2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fileCount").value(2))
                .andExpect(jsonPath("$.infos[0].filePath").value("test1.txt"))
                .andExpect(jsonPath("$.infos[0].contentType").value(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(jsonPath("$.infos[0].textData").value("This is a test file 1"))
                .andExpect(jsonPath("$.infos[1].filePath").value("test2.txt"))
                .andExpect(jsonPath("$.infos[1].contentType").value(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(jsonPath("$.infos[1].textData").value("This is a test file 2"));
    }
}
