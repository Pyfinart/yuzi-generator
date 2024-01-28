package com.pyfinart.maker.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class TemplateMakerFileConfig {

    private List<FileInfoConfig> files;
    private FileGroupConfig fileGroupConfig;

    @NoArgsConstructor
    @Data
    public static class FileInfoConfig{
        /**
         * 文件或路径
         */
        private String path;

        /**
         * 对一个文件或者一个目录下的所有文件，可以有0个或多个过滤条件
         */
        private List<FileFilterConfig> filterConfigList;
    }

    /**
     * 用户可以将执行一次模板制作中用到的所有文件（除去过滤掉的文件）作为一个组
     * 文件组配置
     */
    @Data
    public static class FileGroupConfig{
        private String condition;
        private String groupName;
        private String groupKey;
    }

}
