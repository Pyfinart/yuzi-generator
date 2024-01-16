package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import ${basePackage}.generator.file.FileGenerator;
import ${basePackage}.model.DataModel;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Data
@Command(name = "generate", mixinStandardHelpOptions = true) //mixinStandardHelpOptions = true打开帮助手册
public class GenerateCommand implements Callable<Integer> {

<#list modelConfig.models as modelInfo>
        @CommandLine.Option(names = {<#if modelInfo.abbr??>"-${modelInfo.abbr}", </#if>"--${modelInfo.fieldName}"}, <#if modelInfo.description??>description = "${modelInfo.description}", </#if>arity = "0..1", interactive = true, echo = true)
        private ${modelInfo.type} ${modelInfo.fieldName}<#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;
</#list>

    @Override
    public Integer call() throws TemplateException, IOException {
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
        System.out.println("配置信息：" + dataModel);
        FileGenerator.doGenerate(dataModel);
        return 0;
    }
}
