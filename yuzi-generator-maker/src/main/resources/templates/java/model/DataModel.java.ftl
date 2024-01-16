package ${basePackage}.model;

import lombok.Data;


/**
 * 静态模板配置
 */
@Data
public class DataModel {
<#list modelConfig.models as modelInfo>
<#--modelInfo.description不为空则生成文档注释-->
    <#if modelInfo.description??>
        /**
        * ${modelInfo.description}
        */
    </#if>
    private ${modelInfo.type} ${modelInfo.fieldName} <#if modelInfo.defaultValue??>= ${modelInfo.defaultValue?c}</#if>;
</#list>


}
