package com.pyfinart.maker.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.pyfinart.maker.generator.file.DynamicFileGenerator;
import com.pyfinart.maker.meta.Meta;
import com.pyfinart.maker.meta.MetaManager;
import com.pyfinart.utils.PathUtils;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {

        Meta meta = MetaManager.getMetaObject();
        System.out.println(meta);

        String projectPath = System.getProperty("user.dir");
        String outputBasePath = projectPath + File.separator + "generated";
        if (!FileUtil.exist(outputBasePath)) {
            FileUtil.mkdir(outputBasePath);
        }

        // 读取resource目录
        ClassPathResource classPathResource = new ClassPathResource("");
        String inputResourcePath = classPathResource.getAbsolutePath();

        // 指定输出的java包路径
        // com.pyfinart
        String basePackage = meta.getBasePackage();
        // com/pyfinart
        String outputBasePackagePath = StrUtil.join(File.separator, StrUtil.split(basePackage, "."));
        // src/main/java/com/pyfinart/xxx
        String outputRelativePath = outputBasePath + File.separator + PathUtils.connectPath("src", "main", "java") + File.separator + outputBasePackagePath;


        String inputFilePath;
        String outputFilePath;
        // model.DataModel
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "java", "model", "DataModel.java.ftl");
        outputFilePath = outputRelativePath + File.separator + PathUtils.connectPath("model", "DataModel.java");

        // 生成
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.GenerateCommand
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "java", "cli", "command", "GenerateCommand.java.ftl");
        outputFilePath = outputRelativePath + File.separator + PathUtils.connectPath("cli", "command", "GenerateCommand.java");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.ConfigCommand
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "java", "cli", "command", "ConfigCommand.java.ftl");
        outputFilePath = outputRelativePath + File.separator + PathUtils.connectPath("cli", "command", "ConfigCommand.java");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.ListCommand
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "java", "cli", "command", "ListCommand.java.ftl");
        outputFilePath = outputRelativePath + File.separator + PathUtils.connectPath("cli", "command", "ListCommand.java");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.CommandExecutor
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "java", "cli", "CommandExecutor.java.ftl");
        outputFilePath = outputRelativePath + File.separator + PathUtils.connectPath("cli", "CommandExecutor.java");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // main
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "java", "Main.java.ftl");
        outputFilePath = outputRelativePath + File.separator + PathUtils.connectPath("Main.java");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.DynamicGenerator
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "java", "generator", "DynamicGenerator.java.ftl");
        outputFilePath = outputRelativePath + File.separator + PathUtils.connectPath("generator", "DynamicGenerator.java");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.StaticGenerator
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "java", "generator", "StaticGenerator.java.ftl");
        outputFilePath = outputRelativePath + File.separator + PathUtils.connectPath("generator", "StaticGenerator.java");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.MainGenerator
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "java", "generator", "MainGenerator.java.ftl");
        outputFilePath = outputRelativePath + File.separator + PathUtils.connectPath("generator", "MainGenerator.java");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // pom.xml
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "pom.xml.ftl");
        outputFilePath = outputRelativePath + File.separator + PathUtils.connectPath("pom.xml");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // 构建jar包
        JarGenerator.doGenerator(outputBasePath);

        // 封装脚本
        String shellOutputPath = outputBasePath + File.separator + "generator";
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath = "target/" + jarName; // 这个路径是脚本里面的路径
        ScriptGenerator.doGenerator(shellOutputPath, jarPath);
    }
}
