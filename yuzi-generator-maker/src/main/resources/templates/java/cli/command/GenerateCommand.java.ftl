package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import ${basePackage}.generator.MainGenerator;
import ${basePackage}.model.DataModel;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

<#-- 生成选项 -->
<#macro generateOption indent modelInfo>
${indent}@CommandLine.Option(names = {<#if modelInfo.abbr??>"-${modelInfo.abbr}", </#if>"--${modelInfo.fieldName}"}, <#if modelInfo.description??>description = "${modelInfo.description}", </#if>arity = "0..1", interactive = true, echo = true)
${indent}private ${modelInfo.type} ${modelInfo.fieldName}<#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;
</#macro>

<#macro generateCommand indent modelInfo>
${indent}System.out.println("输入${modelInfo.groupName}配置：");
${indent}CommandLine commandLine = new CommandLine(${modelInfo.type}Command.class);
${indent}commandLine.execute(${modelInfo.allArgsStr});
</#macro>

@Data
@Command(name = "generate", mixinStandardHelpOptions = true) //mixinStandardHelpOptions = true打开帮助手册
public class GenerateCommand implements Callable<Integer> {

<#list modelConfig.models as modelInfo>
<#-- 有分组 -->
    <#if modelInfo.groupKey??>
    /**
     * ${modelInfo.groupName}
     */
    static DataModel.${modelInfo.type} ${modelInfo.groupKey} = new DataModel.${modelInfo.type}();

    <#-- 根据分组生成命令类 -->
    @Command(name = "${modelInfo.groupKey}")
    @Data
    public static class ${modelInfo.type}Command implements Runnable {
        <#list modelInfo.models as subModelInfo>
            <@generateOption indent="        " modelInfo=subModelInfo/>
        </#list>

        @Override
        public void run() {
        <#list modelInfo.models as subModelInfo>
            ${modelInfo.groupKey}.${subModelInfo.fieldName} = ${subModelInfo.fieldName};
        </#list>
        }
    }
    <#else>
    <@generateOption indent="    " modelInfo=modelInfo/>
    </#if>
</#list>
    <#-- 生成调用方法 -->
    @Override
    public Integer call() throws TemplateException, IOException {
    <#list modelConfig.models as modelInfo>
    <#if modelInfo.groupKey??>
    <#if modelInfo.condition??>
        // 根据外层命令参数决定是否调用子命令
        if(${modelInfo.condition}){
            <@generateCommand indent="            " modelInfo=modelInfo/>
        }
    <#else>
        <@generateCommand indent="        " modelInfo=modelInfo/>
    </#if>
    </#if>
    </#list>
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
    <#list modelConfig.models as modelInfo>
    <#if modelInfo.groupKey??>
        dataModel.${modelInfo.groupKey} = ${modelInfo.groupKey};
    </#if>
    </#list>
        System.out.println("配置信息：" + dataModel);
        MainGenerator.doGenerate(dataModel);
        return 0;
    }
}
