package com.pyfinart.maker.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.pyfinart.maker.generator.JarGenerator;
import com.pyfinart.maker.generator.ScriptGenerator;
import com.pyfinart.maker.generator.file.DynamicFileGenerator;
import com.pyfinart.maker.meta.Meta;
import com.pyfinart.maker.meta.MetaManager;
import com.pyfinart.utils.PathUtils;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public abstract class GenerateTemplate {
    public void doGenerate() throws TemplateException, IOException, InterruptedException {
        Meta meta = MetaManager.getMetaObject();

        // 0.输出根路径
        String projectPath = System.getProperty("user.dir");
        String outputBasePath = projectPath + File.separator + "generated" + File.separator + meta.getName();
        if (!FileUtil.exist(outputBasePath)) {
            FileUtil.mkdir(outputBasePath);
        }

        // 1.将模板文件复制到生成的代码生成器中
        String sourceCopyPath = copySource(meta, outputBasePath);

        // 2.代码生成
        generateCode(meta, outputBasePath);

        // 3.构建jar包
        String jarPath = buildJar(meta, outputBasePath);

        // 4.封装脚本
        String shellOutputPath = buildScript(outputBasePath, jarPath);

        // 5.生成精简版程序
        buildDist(outputBasePath, sourceCopyPath, shellOutputPath, jarPath);
    }

    /**
     * 代码生成
     *
     * @param meta           元信息文件封装类
     * @param outputBasePath 非精简版生成的项目的根路径
     * @throws IOException
     * @throws TemplateException
     */
    protected void generateCode(Meta meta, String outputBasePath) throws IOException, TemplateException {
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

        // main，生成器执行入口
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
        outputFilePath = outputBasePath + File.separator + PathUtils.connectPath("pom.xml");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // README.md
        inputFilePath = inputResourcePath + File.separator + PathUtils.connectPath("templates", "README.md.ftl");
        outputFilePath = outputBasePath + File.separator + PathUtils.connectPath("README.md");
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
    }

    /**
     * 复制原始的生成器要用来生成的文件
     * 如/Users/yhj19/developer/MY_Java/yuzi-generator/samples/acm-template-pro
     *
     * @param meta 元信息文件封装类
     * @param outputBasePath 非精简版生成的项目的根路径
     * @return 非精简版生成的项目的代码模板路径
     */
    protected String copySource(Meta meta, String outputBasePath) {
        String sourceRootPath = meta.getFileConfig().getSourceRootPath(); // 原始代码模板的位置（非生成器的模板，是要生成器要生成的代码的模板）
        String sourceCopyPath = outputBasePath + File.separator + ".source";
        FileUtil.copy(sourceRootPath, sourceCopyPath, true);
        return sourceCopyPath;
    }

    /**
     * 构建jar包
     * @param meta 元信息文件封装类
     * @param outputBasePath 非精简版生成的项目的根路径
     * @return jar包相对路径
     * @throws IOException
     * @throws InterruptedException
     */
    protected String buildJar(Meta meta, String outputBasePath) throws IOException, InterruptedException {
        JarGenerator.doGenerator(outputBasePath);
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath = "target/" + jarName; // 这个路径是脚本里面的路径
        return jarPath;
    }

    /**
     * 封装脚本
     * @param outputBasePath 非精简版生成的项目的根路径
     * @param jarPath jar包相对路径
     * @return 非精简版生成的项目的执行脚本
     */
    protected String buildScript(String outputBasePath, String jarPath) {
        String shellOutputPath = outputBasePath + File.separator + "generator";
        ScriptGenerator.doGenerator(shellOutputPath, jarPath);
        return shellOutputPath;
    }

    /**
     * 生成精简版程序
     * @param outputBasePath 非精简版生成的项目的根路径
     * @param sourceCopyPath 非精简版生成的项目的代码模板路径
     * @param shellOutputPath 非精简版生成的项目的执行脚本
     * @param jarPath jar包相对路径
     */
    protected void buildDist(String outputBasePath, String sourceCopyPath, String shellOutputPath, String jarPath) {
        // 生成生产环境精简版的产物包，和之前的对比，去掉了生成器代码目录，只保留生成器代码打包成的jar包
        // 从前面生成的内容复制代码到新的路径
        String distOutputPath = outputBasePath + "-dist";
        FileUtil.mkdir(distOutputPath);
        // 拷贝脚本文件
        FileUtil.copy(shellOutputPath, distOutputPath, true); // linux脚本
        FileUtil.copy(shellOutputPath + ".bat", distOutputPath, true); // windows脚本

        // 拷贝源模板文件
        FileUtil.copy(sourceCopyPath,  distOutputPath, true);

        // copy jar包
        String targetAbsoluteOutputPath = distOutputPath + File.separator + "/target";
        FileUtil.mkdir(targetAbsoluteOutputPath);
        String jarAbsolutePath = outputBasePath + File.separator + jarPath;
        FileUtil.copy(jarAbsolutePath, targetAbsoluteOutputPath, true);
    }
}
