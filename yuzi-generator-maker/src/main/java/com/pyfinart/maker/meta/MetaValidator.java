package com.pyfinart.maker.meta;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.pyfinart.maker.meta.Meta.FileConfigDTO;
import com.pyfinart.maker.meta.Meta.FileConfigDTO.FileDTO;
import com.pyfinart.maker.meta.Meta.ModelConfigDTO;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class MetaValidator {
    public static void doValidateAndFill(Meta meta) {
        metaRootValidateAndFill(meta);
        metaFileConfigValidateAndFill(meta);
        metaModelConfigValidateAndFill(meta);
    }

    private static void metaModelConfigValidateAndFill(Meta meta) {
        ModelConfigDTO modelConfig = meta.getModelConfig();
        // modelConfig校验与填充
        if (modelConfig == null) {
            return;
        }
        List<ModelConfigDTO.ModelDTO> ModelDTOList = modelConfig.getModels();
        if (!CollectionUtil.isNotEmpty(ModelDTOList)) {
            return;
        }
        for (ModelConfigDTO.ModelDTO ModelDTO : ModelDTOList) {
            // 输出路径默认值
            String fieldName = ModelDTO.getFieldName();
            if (StrUtil.isBlank(fieldName)) {
                throw new MetaException("未填写 fieldName");
            }

            String ModelDTOType = ModelDTO.getType();
            if (StrUtil.isEmpty(ModelDTOType)) {
                ModelDTO.setType("String");
            }
        }
    }

    private static void metaFileConfigValidateAndFill(Meta meta) {
        // fileConfig校验与填充
        FileConfigDTO fileConfig = meta.getFileConfig();

        if (fileConfig == null) {
            return;
        }
        List<FileDTO> fileInfoList = fileConfig.getFiles();
        String sourceRootPath = fileConfig.getSourceRootPath();
        // sourceRootPath必填
        if (StrUtil.isBlank(sourceRootPath)) {
            throw new MetaException("未填写 sourceRootPath");
        }

        // inputRootPath，默认为.source/+sourceRootPath的最后一个层级
        String defaultInputRootPath = ".source" + File.separator +
                FileUtil.getLastPathEle(Paths.get(sourceRootPath)).getFileName().toString();
        String inputRootPath = StrUtil.blankToDefault(fileConfig.getInputRootPath(), defaultInputRootPath);
        fileConfig.setInputRootPath(inputRootPath);

        // outputRootPath，默认为当前目录下的generated目录
        String outputRootPath = StrUtil.blankToDefault(fileConfig.getOutputRootPath(), "generated");
        fileConfig.setOutputRootPath(outputRootPath);

        // type
        String fileConfigType = StrUtil.blankToDefault(fileConfig.getType(), "dir");
        fileConfig.setType(fileConfigType);

        // fileInfoList
        if (!CollectionUtil.isNotEmpty(fileInfoList)) {
            return;
        }
        for (FileDTO fileInfo : fileInfoList) {
            String inputPath = fileInfo.getInputPath();
            String outputPath = fileInfo.getOutputPath();
            String type = fileInfo.getType();
            String generateType = fileInfo.getGenerateType();
            // inputPath 必填
            if (StrUtil.isBlank(inputPath)) {
                throw new MetaException("未填写 inputPath");
            }
            // outputPath: 默认等于 inputPath
            if (StrUtil.isEmpty(outputPath)) {
                fileInfo.setOutputPath(inputPath);
            }
            // type：默认 inputPath 有文件后缀（如 .java）为 file，否则为 dir
            if (StrUtil.isBlank(type)) {
                // 无文件后缀
                if (StrUtil.isBlank(FileUtil.getSuffix(inputPath))) {
                    fileInfo.setType("dir");
                } else {
                    fileInfo.setType("file");
                }
            }
            // generateType：如果文件结尾不为 Ftl，generateType 默认为 static，否则为 dynamic
            if (StrUtil.isBlank(generateType)) {
                // 为动态模板
                if (inputPath.endsWith(".ftl")) {
                    fileInfo.setGenerateType("dynamic");
                } else {
                    fileInfo.setGenerateType("static");
                }
            }

        }
    }

    private static void metaRootValidateAndFill(Meta meta) {
        // 校验并填充默认值
        String name = StrUtil.blankToDefault(meta.getName(), "my-generator");
        String description = StrUtil.emptyToDefault(meta.getDescription(), "我的模板代码生成器");
        String author = StrUtil.emptyToDefault(meta.getAuthor(), "yupi");
        String basePackage = StrUtil.blankToDefault(meta.getBasePackage(), "com.yupi");
        String version = StrUtil.emptyToDefault(meta.getVersion(), "1.0");
        String createTime = StrUtil.emptyToDefault(meta.getCreateTime(), DateUtil.now());
        meta.setName(name);
        meta.setDescription(description);
        meta.setAuthor(author);
        meta.setBasePackage(basePackage);
        meta.setVersion(version);
        meta.setCreateTime(createTime);
    }
}
