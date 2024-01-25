package com.pyfinart.maker.template;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.pyfinart.maker.template.enums.FileFilterRangeEnum;
import com.pyfinart.maker.template.enums.FileFilterRuleEnum;
import com.pyfinart.maker.template.model.FileFilterConfig;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class FileFilter {

    /**
     * 单个文件过滤
     *
     * @param fileFilterConfigs 过滤规则
     * @param file              单个文件
     * @return 是否保留 true保留
     */
    public static boolean doSingleFilter(List<FileFilterConfig> fileFilterConfigs, File file) {
        String fileName = file.getName();
        String fileContent = FileUtil.readUtf8String(file);

        // 是否保留
        boolean res = true;

        if (CollectionUtil.isEmpty(fileFilterConfigs)) return true;

        for (FileFilterConfig fileFilterConfig : fileFilterConfigs) {
            String rule = fileFilterConfig.getRule();
            String value = fileFilterConfig.getValue();
            String range = fileFilterConfig.getRange();

            FileFilterRangeEnum fileFilterRangeEnum = FileFilterRangeEnum.getEnumByValue(range);

            if (fileFilterRangeEnum == null) continue; // 无规则则直接跳过

            // 要过滤的内容
            String content = fileName;
            switch (fileFilterRangeEnum){
                case FILE_NAME:
                    content = fileName;
                    break;
                case FILE_CONTENT:
                    content = fileContent;
                    break;
                default:
            }

            FileFilterRuleEnum fileFilterRuleEnum = FileFilterRuleEnum.getEnumByValue(rule);
            if(fileFilterRuleEnum == null) continue;

            switch (fileFilterRuleEnum){
                case REGEX:
                    res = content.matches(value);
                    break;
                case CONTAINS:
                    res = content.contains(value);
                    break;
                case EQUALS:
                    res = content.equals(value);
                    break;
                case ENDS_WITH:
                    res = content.endsWith(value);
                    break;
                case STARTS_WITH:
                    res = content.startsWith(value);
                    break;
                default:
            }

            // 有一个fileFilterConfigs的过滤条件不满足，直接返回
            if(!res) return false;
        }

        // 都满足，返回true
        return true;
    }

    /**
     * 对某个文件或目录进行过滤，返回文件列表
     * @param filePath
     * @param fileFilterConfigs
     * @return
     */
    public static List<File> doFilter(String filePath, List<FileFilterConfig> fileFilterConfigs){
        // 根据路径获取所有文件
        List<File> files = FileUtil.loopFiles(filePath);

        return files.stream()
                .filter(file -> doSingleFilter(fileFilterConfigs, file))
                .collect(Collectors.toList());
    }
}
