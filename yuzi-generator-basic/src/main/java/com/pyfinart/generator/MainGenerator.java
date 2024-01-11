package com.pyfinart.generator;

import com.pyfinart.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {
    public static void doGenerate(MainTemplateConfig mainTemplateConfig) throws TemplateException, IOException {
        // 1.静态文件生成
        String projectRoot = System.getProperty("user.dir");

        // 因为生成器要用的项目acm-template在yuzi-generator-basic目录的上一级目录中
        String parentPathName = new File(projectRoot).getParentFile().getAbsolutePath();
        // 输入路径
        String inputPath = parentPathName + File.separator + "samples" + File.separator + "acm-template";
        // 输出路径
        String outputPath = projectRoot;

        StaticGenerator.copyFileByHutool(inputPath, outputPath);

        // 2.动态文件生成
        String dynamicInputPath = projectRoot + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String dynamicOutputPath = projectRoot + File.separator + "acm-template/src/com/yupi/acm/MainTemplate.java";

//        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
//        mainTemplateConfig.setAuthor("Ruan");
//        mainTemplateConfig.setLoop(true);
//        mainTemplateConfig.setOutput("求和结果：");
        DynamicGenerator.doGenerate(dynamicInputPath, dynamicOutputPath, mainTemplateConfig);
    }
}
