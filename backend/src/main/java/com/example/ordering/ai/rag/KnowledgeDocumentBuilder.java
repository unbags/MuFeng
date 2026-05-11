package com.example.ordering.ai.rag;

import com.example.ordering.dto.DishResponse;
import com.example.ordering.dto.MenuResponse;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
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
     * 将空值统一转换为知识库中的中文占位说明。
     */
    private String value(Object value) {
        return value == null ? "未提供" : String.valueOf(value);
    }
}
