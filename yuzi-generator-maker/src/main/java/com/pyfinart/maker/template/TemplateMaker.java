package com.pyfinart.maker.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.pyfinart.maker.meta.Meta;
import com.pyfinart.maker.meta.enums.FileGenerateTypeEnum;
import com.pyfinart.maker.meta.enums.FileTypeEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * FTL模板和meta元信息文件 制作工具，使用字符串替换实现
 */
public class TemplateMaker {
    public static void main(String[] args) {
        // 一。输入信息
        // 1. 挖坑的信息
        String name = "acm-template-generator";
        String description = "ACM 示例模板生成器";

        // 2. 输入文件的信息
        String projectPath = System.getProperty("user.dir");
        String sourcePath = new File(projectPath).getParent() + File.separator + "samples/acm-template";
        String fileInputPath = "src/com/yupi/acm/MainTemplate.java"; // 因为有了sourcePath，文件路径只需要相对路径
        String fileOutputPath = fileInputPath + ".ftl";

        // 3. 模型参数的信息(这里是MainTemplate的往坑参数)
        Meta.ModelConfigDTO.ModelDTO modelDTO = new Meta.ModelConfigDTO.ModelDTO();
        modelDTO.setFieldName("output");
        modelDTO.setType("String");
        modelDTO.setDefaultValue("Sum: ");

        // 二、 使用字符串替换生成模板文件
        // 1. 挖坑
        String fileAbsoluteInputPath = sourcePath + File.separator + fileInputPath;
        String fileContent = FileUtil.readUtf8String(fileAbsoluteInputPath);
        String replacement = String.format("${%s}", modelDTO.getFieldName());
        String newFileContent = StrUtil.replace(fileContent, "Sum: ", replacement);

        // 2. 写入模板文件
        String absoluteOutputPath = sourcePath + File.separator + fileOutputPath;
        FileUtil.writeUtf8String(newFileContent, absoluteOutputPath);

        // 三、生成元信息配置文件
        // 实现思路，首先在程序中实例化Meta类，并填充参数，在写入文件
        String metaOutputPath = sourcePath + File.separator + "meta.json";

        // 1. 构造配置参数
        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);

        Meta.FileConfigDTO fileConfigDTO = new Meta.FileConfigDTO();
        meta.setFileConfig(fileConfigDTO);
        fileConfigDTO.setSourceRootPath(sourcePath);

        List<Meta.FileConfigDTO.FileDTO> fileDTOList = new ArrayList<>();
        fileConfigDTO.setFiles(fileDTOList);

        Meta.FileConfigDTO.FileDTO fileDTO = new Meta.FileConfigDTO.FileDTO();
        fileDTO.setInputPath(fileInputPath);
        fileDTO.setOutputPath(fileOutputPath);
        fileDTO.setType(FileTypeEnum.FILE.getValue());
        fileDTO.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
        fileDTOList.add(fileDTO);

        Meta.ModelConfigDTO modelConfig = new Meta.ModelConfigDTO();
        meta.setModelConfig(modelConfig);

        List<Meta.ModelConfigDTO.ModelDTO> modelInfoList = new ArrayList<>();
        modelConfig.setModels(modelInfoList);
        modelInfoList.add(modelDTO);

        // 2. 输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr (meta), metaOutputPath);
    }

}
