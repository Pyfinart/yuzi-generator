package com.pyfinart.maker.meta;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.pyfinart.maker.meta.Meta.FileConfigDTO;
import com.pyfinart.maker.meta.Meta.FileConfigDTO.FileDTO;
import com.pyfinart.maker.meta.Meta.ModelConfigDTO;
import com.pyfinart.maker.meta.enums.FileGenerateTypeEnum;
import com.pyfinart.maker.meta.enums.FileTypeEnum;
import com.pyfinart.maker.meta.enums.ModelTypeEnum;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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
        for (ModelConfigDTO.ModelDTO modelDTO : ModelDTOList) {
            if (!StrUtil.isEmpty(modelDTO.getGroupKey())) {
                // 处理中间参数allArgsStr: "--author", "--output"
                List<ModelConfigDTO.ModelDTO> models = modelDTO.getModels();
                String allArgsStr = models.stream()
                        .map(subModel -> String.format("\"--%s\"", subModel.getFieldName()))
                        .collect(Collectors.joining(", "));
                modelDTO.setAllArgsStr(allArgsStr);

                for (ModelConfigDTO.ModelDTO model : modelDTO.getModels()) {
                    // 字段名
                    String fieldName = model.getFieldName();
                    if (StrUtil.isBlank(fieldName)) {
                        throw new MetaException("未填写 fieldName");
                    }

                    String blankToDefault = StrUtil.blankToDefault(model.getType(), ModelTypeEnum.STRING.getValue());
                    model.setType(blankToDefault);
                }
            } else {
                // 字段名
                String fieldName = modelDTO.getFieldName();
                if (StrUtil.isBlank(fieldName)) {
                    throw new MetaException("未填写 fieldName");
                }

                String blankToDefault = StrUtil.blankToDefault(modelDTO.getType(), ModelTypeEnum.STRING.getValue());
                modelDTO.setType(blankToDefault);
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
        String fileConfigType = StrUtil.blankToDefault(fileConfig.getType(), FileTypeEnum.DIR.getValue());
        fileConfig.setType(fileConfigType);

        // fileInfoList
        if (!CollectionUtil.isNotEmpty(fileInfoList)) {
            return;
        }
        for (FileDTO fileInfo : fileInfoList) {
            String inputPath = fileInfo.getInputPath();
            String type = fileInfo.getType();
            String generateType = fileInfo.getGenerateType();
            // 类型为 group 不做校验
            if (FileTypeEnum.GROUP.getValue().equals(type)) {
                continue;
            }
            // inputPath 必填
            if (StrUtil.isBlank(inputPath)) {
                throw new MetaException("未填写 inputPath");
            }
            // outputPath: 默认等于 inputPath
            String blankToDefault = StrUtil.blankToDefault(fileInfo.getOutputPath(), inputPath);
            fileInfo.setOutputPath(blankToDefault);

            // type：默认 inputPath 有文件后缀（如 .java）为 file，否则为 dir
            if (StrUtil.isBlank(type)) {
                // 无文件后缀
                if (StrUtil.isBlank(FileUtil.getSuffix(inputPath))) {
                    fileInfo.setType(FileTypeEnum.DIR.getValue());
                } else {
                    fileInfo.setType(FileTypeEnum.FILE.getValue());
                }
            }
            // generateType：如果文件结尾不为 Ftl，generateType 默认为 static，否则为 dynamic
            if (StrUtil.isBlank(generateType)) {
                // 为动态模板
                if (inputPath.endsWith(".ftl")) {
                    fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
                } else {
                    fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
                }
            }

        }
    }

    private static void metaRootValidateAndFill(Meta meta) {
        // 校验并填充默认值
        String name = StrUtil.blankToDefault(meta.getName(), "my-generator");
        String description = StrUtil.emptyToDefault(meta.getDescription(), "我的模板代码生成器");
        String author = StrUtil.emptyToDefault(meta.getAuthor(), "ruan");
        String basePackage = StrUtil.blankToDefault(meta.getBasePackage(), "com.pyfinart");
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
