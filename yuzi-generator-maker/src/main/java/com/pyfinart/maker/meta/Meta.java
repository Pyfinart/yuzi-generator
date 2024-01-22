package com.pyfinart.maker.meta;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class Meta {

    private String name;
    private String description;
    private String basePackage;
    private String version;
    private String author;
    private String createTime;
    private FileConfigDTO fileConfig;
    private ModelConfigDTO modelConfig;

    @NoArgsConstructor
    @Data
    public static class FileConfigDTO {
        private String inputRootPath;
        private String outputRootPath;
        private String sourceRootPath;
        private String type;
        private List<FileDTO> files;

        @NoArgsConstructor
        @Data
        public static class FileDTO {
            private String inputPath;
            private String outputPath;
            private String type;
            private String generateType;
            private String condition;
            private String groupKey;
            private String groupName;
            private List<FileDTO> files;
        }
    }

    @NoArgsConstructor
    @Data
    public static class ModelConfigDTO {
        private List<ModelDTO> models;

        @NoArgsConstructor
        @Data
        public static class ModelDTO {
            private String fieldName;
            private String type;
            private String description;
            private Object defaultValue;
            private String abbr;
        }
    }
}
