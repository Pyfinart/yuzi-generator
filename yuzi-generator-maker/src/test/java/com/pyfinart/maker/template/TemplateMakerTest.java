package com.pyfinart.maker.template;

import com.pyfinart.maker.meta.Meta;
import com.pyfinart.maker.template.enums.FileFilterRangeEnum;
import com.pyfinart.maker.template.enums.FileFilterRuleEnum;
import com.pyfinart.maker.template.model.FileFilterConfig;
import com.pyfinart.maker.template.model.TemplateMakerFileConfig;
import com.pyfinart.maker.template.model.TemplateMakerModelConfig;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.pyfinart.maker.template.TemplateMaker.makeTemplate;

public class TemplateMakerTest {

    @Test
    public void makeTemplateTest() {

        String name = "acm-template-generator";
        String description = "ACM 示例模板生成器";
        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);

        // 3. 模型参数的信息(这里是MainTemplate的挖坑参数)
//        Meta.ModelConfigDTO.ModelDTO modelDTO = new Meta.ModelConfigDTO.ModelDTO();
//        modelDTO.setFieldName("output");
//        modelDTO.setType("String");
//        modelDTO.setDefaultValue("Sum: ");

        // 模型参数信息（第二次）
        Meta.ModelConfigDTO.ModelDTO modelDTO = new Meta.ModelConfigDTO.ModelDTO();
        modelDTO.setFieldName("className");
        modelDTO.setType("String");

        String projectPath = System.getProperty("user.dir");
        String originalProjectPath = new File(projectPath).getParent() + File.separator + "samples/springboot-init";

//        String searchStr = "Sum: ";

        // 替换变量（第二次）
        String searchStr = "BaseResponse";

        String inputFilePath1 = "src/main/java/com/yupi/springbootinit/common";  // 因为方法中有了sourcePath，文件路径只需要相对路径
        String inputFilePath2 = "src/main/resources/application.yml";

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();

        // 模型组配置
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        modelGroupConfig.setGroupKey("mysql");
        modelGroupConfig.setGroupName("数据库配置");
        templateMakerModelConfig.setModelGroupConfig(modelGroupConfig);

        // 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");

        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig2 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig2.setFieldName("username");
        modelInfoConfig2.setType("String");
        modelInfoConfig2.setDefaultValue("root");
        modelInfoConfig2.setReplaceText("root");

        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig1, modelInfoConfig2);
        templateMakerModelConfig.setModels(modelInfoConfigList);

        // 文件过滤
        // 只处理 common 包下文件名称包含 Base 的文件和 controller 包下的文件。
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(inputFilePath1);
        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
        FileFilterConfig fileFilterConfig = FileFilterConfig.builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .value("Base")
                .build();
        fileFilterConfigList.add(fileFilterConfig);
        fileInfoConfig1.setFilterConfigList(fileFilterConfigList);

        // 分组配置
        // 分组1
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
        fileGroupConfig.setCondition("output输出");
        fileGroupConfig.setGroupName("testGroup2");
        fileGroupConfig.setGroupKey("测试分组");
        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);
        // 分组2
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(inputFilePath2);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1, fileInfoConfig2));

        long l = makeTemplate(meta, templateMakerModelConfig, originalProjectPath, templateMakerFileConfig, 1750752428187742208l);
        System.out.println(l);
    }

    @Test
    public void testModelError() {
        Meta meta = new Meta();
        // 基本配置
        meta.setName("acm-template-generator");
        meta.setDescription("ACM 示例模板生成器");

        // 原始文件路径
        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParent() + File.separator + "samples/springboot-init";

        // 文件参数配置
        String inputFilePath1 = "src/main/resources/application.yml";
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(inputFilePath1);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1));

        // 模型配置参数
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setDescription("MySQL地址");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");
        templateMakerModelConfig.setModels(Arrays.asList(modelInfoConfig1));

        long l = makeTemplate(
                meta,
                templateMakerModelConfig,
                originProjectPath,
                templateMakerFileConfig,
                null
        );

    }

    @Test
    public void testBug2() {
        Meta meta = new Meta();
        // 基本配置
        meta.setName("acm-template-generator");
        meta.setDescription("ACM 示例模板生成器");

        // 原始文件路径
        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParent() + File.separator + "samples/springboot-init";

        // 文件参数配置
        String inputFilePath1 = "src/main/java/com/yupi/springbootinit/common";
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(inputFilePath1);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1));

        // 模型配置参数
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("className");
//        modelInfoConfig1.setDescription("MySQL地址");
//        modelInfoConfig1.setDefaultValue("BaseResponse");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setReplaceText("BaseResponse");
        templateMakerModelConfig.setModels(Arrays.asList(modelInfoConfig1));

        long l = makeTemplate(
                meta,
                templateMakerModelConfig,
                originProjectPath,
                templateMakerFileConfig,
                1l
        );

    }
}