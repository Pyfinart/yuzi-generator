package com.pyfinart.maker.generator.file;

import cn.hutool.core.io.FileUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 动态文件生成器
 */
public class DynamicFileGenerator {

    /**
     * 生成文件
     *
     * @param inputPath  模板文件输入路径
     * @param outputPath 输出路径，是一个文件的路径,非目录路径
     * @param model      数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException {

        // 如果文件不存在，则新建，注意是文件，所以不用mkdir
        if(!FileUtil.exist(outputPath)){
            FileUtil.touch(outputPath);
        }

        try(Writer out = new FileWriter(outputPath)) { // 自动关闭资源

            // new 出 Configuration 对象，参数为 FreeMarker 版本号
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

            File templateDir = new File(inputPath).getParentFile(); // 将模板所在目录作为模板目录

            // 指定模板文件所在的路径
            configuration.setDirectoryForTemplateLoading(templateDir);

            // 设置模板文件使用的字符集
            configuration.setDefaultEncoding("utf-8");

            configuration.setNumberFormat("0.######"); // 使数字格式不带符号如2,023

            // 创建模板对象，加载指定模板
            String templateName = new File(inputPath).getName(); // 模板对象的名字
            Template template = configuration.getTemplate(templateName);

            //输出，生成文件在根目录
            template.process(model, out);
        }

    }
}
