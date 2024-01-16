package ${basePackage}.generator;

import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {
    public static void doGenerate(Object model) throws TemplateException, IOException {


        // 生成动态文件
        String intputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";

        String inputPath;
        String outputPath;

<#list fileConfig.files as fileInfo>
        inputPath = new File(intputRootPath, "${fileInfo.inputPath}").getAbsolutePath();
        outputPath = new File(outputRootPath, "${fileInfo.outputPath}").getAbsolutePath();
    <#if fileInfo.generateType == "static">
        // 生成静态文件
        StaticGenerator.copyFileByHutool(inputPath, outputPath);
    <#else>
        // 生成动态文件
        DynamicGenerator.doGenerate(inputPath, outputPath, model);
    </#if>
</#list>
    }
}
