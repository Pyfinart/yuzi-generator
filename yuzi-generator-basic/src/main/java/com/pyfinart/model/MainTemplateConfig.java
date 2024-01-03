package com.pyfinart.model;

import lombok.Data;


/**
 * 静态模板配置
 */
@Data
public class MainTemplateConfig {
    // 需求
    //在代码开头增加作者 @Author 注释（增加代码）
    //修改程序输出的信息提示（替换代码）
    //将循环读取输入改为单次读取（可选代码）

    /**
     * 作者（填充值）
     * 默认值：Ruan
     */
    private String author = "Ruan";

    /**
     * 输出信息
     */
    private String output = "输出结果";

    /**
     * 是否循环（开关）
     */
    private boolean loop = false;
}
