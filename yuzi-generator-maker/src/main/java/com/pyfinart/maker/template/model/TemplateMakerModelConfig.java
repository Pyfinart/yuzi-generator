package com.pyfinart.maker.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class TemplateMakerModelConfig {
    private List<ModelInfoConfig> models;
    private ModelGroupConfig modelGroupConfig;

    @Data
    @NoArgsConstructor
    public static class ModelInfoConfig {
        private String fieldName; // 在ftl文件中是${fieldName}，用于命名挖坑的位置
        private String description;
        private Object defaultValue;
        private String type;
        private String abbr;

        // 用于替换哪些文本
        private String replaceText;
    }

    @Data
    public static class ModelGroupConfig {
        private String condition;
        private String groupName;
        private String groupKey;
    }

}
