package com.example.ordering.ai.rag;

import com.example.ordering.dto.DishResponse;
import com.example.ordering.dto.MenuResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class KnowledgeDocumentBuilder {

    /**
     * 将当前菜单转换为可写入向量库的知识文档列表。
     */
    public List<KnowledgeDocument> fromMenu(MenuResponse menu) {
        if (menu == null || menu.getDishes() == null) {
            return List.of();
        }
        return menu.getDishes().stream()
            .map(this::fromDish)
            .toList();
    }

    /**
     * 将单个菜品转换为包含正文和元数据的知识文档。
     */
    private KnowledgeDocument fromDish(DishResponse dish) {
        String content = String.join("\n",
            "菜品：" + value(dish.getName()),
            "分类：" + value(dish.getCategory()),
            "价格：" + value(dish.getPrice()),
            "亮点：" + value(dish.getHighlight()),
            "描述：" + value(dish.getDescription()),
            "热量：" + value(dish.getCalories())
        );

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("doc_type", "dish");
        metadata.put("tenant_id", "default");
        metadata.put("source_id", "dish:" + dish.getId());
        metadata.put("version", "menu:" + OffsetDateTime.now());
        metadata.put("status", "active");
        metadata.put("updated_at", OffsetDateTime.now().toString());

        return new KnowledgeDocument(content, metadata);
    }

    /**
     * 中文章节标题到英文 slug 的映射，用于生成 source_id。
     */
    private static final Map<String, String> HEADING_SLUGS = Map.ofEntries(
        Map.entry("一、菜品咨询", "dish-consultation"),
        Map.entry("二、下单帮助", "ordering-help"),
        Map.entry("三、订单查询", "order-query"),
        Map.entry("四、支付", "payment"),
        Map.entry("五、外带取餐", "takeout-pickup"),
        Map.entry("六、退款与投诉", "refund-complaint"),
        Map.entry("七、营业信息", "business-info"),
        Map.entry("八、引导与兜底", "guide-fallback")
    );

    /**
     * 读取 classpath 下的客服知识 markdown 文件，按 ## 标题分块，
     * 每块作为一个 KnowledgeDocument 写入向量库。
     */
    public List<KnowledgeDocument> fromMarkdownResource() throws IOException {
        ClassPathResource resource = new ClassPathResource("knowledge/knowledge-base-customer-service.md");
        String fullContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // 按 ## 二级标题拆分
        String[] blocks = fullContent.split("\\n(?=## )");
        List<KnowledgeDocument> documents = new ArrayList<>();

        for (String block : blocks) {
            String trimmed = block.strip();
            if (trimmed.isEmpty() || !trimmed.startsWith("## ")) {
                continue;
            }

            // 提取标题行和正文
            int newlineIdx = trimmed.indexOf('\n');
            String headingLine = newlineIdx > 0 ? trimmed.substring(0, newlineIdx) : trimmed;
            String heading = headingLine.substring(3).trim(); // 去掉 "## "
            String body = newlineIdx > 0 ? trimmed.substring(newlineIdx + 1).trim() : "";

            // 跳过附录等非知识章节
            if (heading.startsWith("附录")) {
                continue;
            }

            String content = heading + "\n" + body;
            String sourceId = "biz:" + headingToSlug(heading);

            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("doc_type", "business_knowledge");
            metadata.put("tenant_id", "default");
            metadata.put("source_id", sourceId);
            metadata.put("status", "active");
            metadata.put("updated_at", OffsetDateTime.now().toString());

            documents.add(new KnowledgeDocument(content, metadata));
        }

        return documents;
    }

    /**
     * 将中文章节标题映射为英文 slug，未匹配时使用标题本身的 hash。
     */
    private String headingToSlug(String heading) {
        return HEADING_SLUGS.getOrDefault(heading, "unknown-" + Math.abs(heading.hashCode()));
    }

    /**
     * 将空值统一转换为知识库中的中文占位说明。
     */
    private String value(Object value) {
        return value == null ? "未提供" : String.valueOf(value);
    }
}
