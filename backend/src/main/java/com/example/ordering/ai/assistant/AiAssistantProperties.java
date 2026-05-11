package com.example.ordering.ai.assistant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiAssistantProperties {

    private boolean enabled;
    private Rag rag = new Rag();
    private Tools tools = new Tools();
    private Prompt prompt = new Prompt();

    public AssistantMode resolveMode() {
        if (!enabled) {
            return AssistantMode.RULE_BASED;
        }
        if (rag.enabled && tools.enabled) {
            return AssistantMode.FULL_AI;
        }
        if (rag.enabled) {
            return AssistantMode.RAG_ONLY;
        }
        return AssistantMode.RULE_BASED;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Rag getRag() {
        return rag;
    }

    public void setRag(Rag rag) {
        this.rag = rag;
    }

    public Tools getTools() {
        return tools;
    }

    public void setTools(Tools tools) {
        this.tools = tools;
    }

    public Prompt getPrompt() {
        return prompt;
    }

    public void setPrompt(Prompt prompt) {
        this.prompt = prompt;
    }

    public static class Rag {
        private boolean enabled;
        private int topK = 5;
        private double similarityThreshold = 0.65d;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public double getSimilarityThreshold() {
            return similarityThreshold;
        }

        public void setSimilarityThreshold(double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
        }
    }

    public static class Tools {
        private boolean enabled;
        private boolean auditEnabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAuditEnabled() {
            return auditEnabled;
        }

        public void setAuditEnabled(boolean auditEnabled) {
            this.auditEnabled = auditEnabled;
        }
    }

    public static class Prompt {
        private String version = "v1";
        private String location = "classpath:/prompts";

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }
}
