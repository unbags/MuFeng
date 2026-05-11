package com.example.ordering.service;

import com.example.ordering.ai.rag.KnowledgeDocument;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class DocumentParsingService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParsingService.class);

    private static final int CHUNK_SIZE = 800;
    private static final int CHUNK_OVERLAP = 150;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "pdf", "docx", "doc", "md", "txt", "markdown", "html", "htm", "csv"
    );

    private final Tika tika = new Tika();

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 10MB，当前: " + (file.getSize() / (1024 * 1024)) + "MB");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("无法识别的文件类型");
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("不支持的文件格式: ." + ext + "，支持: PDF, DOCX, DOC, MD, TXT");
        }
    }

    public String extractText(MultipartFile file) {
        try (java.io.InputStream in = file.getInputStream()) {
            String result = tika.parseToString(in).trim();
            if (result.isEmpty()) {
                throw new IllegalArgumentException("文件内容为空，无法提取文本");
            }
            return result;
        } catch (java.io.IOException e) {
            log.error("Failed to read file stream: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("无法读取文件: " + file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Failed to extract text from file: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("无法解析文件内容: " + file.getOriginalFilename() + " — " + e.getMessage());
        }
    }

    public List<KnowledgeDocument> parse(MultipartFile file) {
        String text = extractText(file);
        String filename = file.getOriginalFilename();
        String fileType = filename != null && filename.contains(".")
            ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
            : "unknown";
        List<String> chunks = chunk(text);
        List<KnowledgeDocument> documents = new ArrayList<>(chunks.size());
        int total = chunks.size();
        for (int i = 0; i < total; i++) {
            Map<String, Object> metadata = buildMetadata(filename != null ? filename : "unknown", fileType, i, total);
            documents.add(new KnowledgeDocument(chunks.get(i), metadata));
        }
        return documents;
    }

    private List<String> chunk(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            if (end < text.length()) {
                int breakPoint = findBreakPoint(text, end, start + overlap);
                if (breakPoint > start) {
                    end = breakPoint;
                }
            }
            chunks.add(text.substring(start, end).trim());
            if (end >= text.length()) break;
            start = end - overlap;
            if (start < 0) start = 0;
        }
        return chunks;
    }

    private List<String> chunk(String text) {
        return chunk(text, CHUNK_SIZE, CHUNK_OVERLAP);
    }

    private int findBreakPoint(String text, int end, int minPos) {
        for (int i = end; i > minPos; i--) {
            char c = text.charAt(i);
            if (c == '\n' || c == '。' || c == '！' || c == '？' || c == '.' || c == '!' || c == '?') {
                return i + 1;
            }
        }
        for (int i = end; i > minPos; i--) {
            char c = text.charAt(i);
            if (c == ' ' || c == '，' || c == ',' || c == '；' || c == ';') {
                return i + 1;
            }
        }
        return end;
    }

    private Map<String, Object> buildMetadata(String filename, String fileType, int chunkIndex, int totalChunks) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("doc_type", "file_upload");
        metadata.put("tenant_id", "default");
        metadata.put("source_type", fileType);
        metadata.put("original_filename", filename);
        metadata.put("chunk_index", chunkIndex);
        metadata.put("total_chunks", totalChunks);
        metadata.put("source_id", filename + ":" + chunkIndex);
        metadata.put("uploaded_at", OffsetDateTime.now().toString());
        metadata.put("status", "active");
        return metadata;
    }
}
