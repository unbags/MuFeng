package com.example.ordering.controller;

import com.example.ordering.ai.rag.KnowledgeDocument;
import com.example.ordering.ai.rag.KnowledgeIngestionService;
import com.example.ordering.dto.ApiResponse;
import com.example.ordering.dto.KnowledgeUploadResult;
import com.example.ordering.service.DocumentParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/admin/knowledge")
@ConditionalOnBean(VectorStore.class)
public class KnowledgeController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeController.class);

    private final DocumentParsingService parsingService;
    private final KnowledgeIngestionService ingestionService;

    public KnowledgeController(DocumentParsingService parsingService,
                               KnowledgeIngestionService ingestionService) {
        this.parsingService = parsingService;
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    public ApiResponse<List<KnowledgeUploadResult>> upload(
            @RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ApiResponse.success("未选择文件", List.of());
        }

        List<KnowledgeUploadResult> results = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            String fileType = "unknown";
            if (filename != null && filename.contains(".")) {
                fileType = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            }
            try {
                parsingService.validate(file);
                List<KnowledgeDocument> chunks = parsingService.parse(file);
                ingestionService.ingest(chunks);
                results.add(KnowledgeUploadResult.success(filename, fileType, chunks.size()));
                log.info("Knowledge file ingested: {} ({} chunks)", filename, chunks.size());
            } catch (Exception e) {
                log.error("Failed to ingest file: {}", filename, e);
                results.add(KnowledgeUploadResult.failure(filename, fileType, e.getMessage()));
            }
        }
        return ApiResponse.success("上传处理完成", results);
    }
}
