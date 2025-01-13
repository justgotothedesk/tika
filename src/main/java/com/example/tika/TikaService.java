package com.example.tika;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class TikaService {
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
