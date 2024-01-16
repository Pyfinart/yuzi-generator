package com.pyfinart.generator;

import com.pyfinart.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {
    public static void doGenerate(Object model) throws TemplateException, IOException {


        // 生成动态文件
        String intputRootPath ="/Users/yhj19/developer/MY_Java/yuzi-generator/samples/acm-template-pro";
        String outputRootPath ="/Users/yhj19/developer/MY_Java/yuzi-generator";

        String inputPath;
        String outputPath;

        inputPath = new File(intputRootPath, "src/com/yupi/acm/MainTemplate.java.ftl").getAbsolutePath();
        outputPath = new File(outputRootPath, "src/com/pyfinart/acm/MainTemplate.java").getAbsolutePath();
        DynamicGenerator.doGenerate(inputPath, outputPath, model);

        // 生成静态文件
        inputPath = new File(intputRootPath, ".gitignore").getAbsolutePath();
        outputPath = new File(outputRootPath, "src/.gitignore").getAbsolutePath();
        StaticGenerator.copyFileByHutool(inputPath, outputPath);

        inputPath = new File(intputRootPath, "README.md").getAbsolutePath();
        outputPath = new File(outputRootPath, "src/README.md").getAbsolutePath();
        StaticGenerator.copyFileByHutool(inputPath, outputPath);



    }

    public static void main(String[] args) throws TemplateException, IOException {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("Ruan");
        mainTemplateConfig.setLoop(true);
        mainTemplateConfig.setOutput("summmmm = ");
        doGenerate(mainTemplateConfig);
    }
}
