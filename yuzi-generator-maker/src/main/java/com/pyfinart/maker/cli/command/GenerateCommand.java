package com.pyfinart.maker.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.pyfinart.maker.generator.file.FileGenerator;
import com.pyfinart.maker.model.DataModel;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.util.concurrent.Callable;

@Data
@Command(name = "generate", mixinStandardHelpOptions = true) //mixinStandardHelpOptions = true打开帮助手册
public class GenerateCommand implements Callable<Integer> {

    /**
     * 是否循环（开关）
     */
    @CommandLine.Option(names = {"-l", "--loop"}, description = "是t否循环", arity = "0..1", interactive = true, echo = true)
    private boolean loop = false;

    /**
     * 作者（填充值）
     * 默认值：Ruan
     */
    // arity="0..1"：在命令行中接收0到1个参数是，若接收到1个则不触发交互，如果接收到0个，则触发交互，提示用户输入
    @CommandLine.Option(names = {"-a", "--author"}, description = "作者", arity = "0..1", interactive = true, echo = true)
    private String author = "Ruan";

    /**
     * 输出信息
     */
    @CommandLine.Option(names = {"-o", "--output"}, description = "输出结果", arity = "0..1", interactive = true, echo = true)
    private String output = "输出结果";

    @Override
    public Integer call() throws TemplateException, IOException {
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
        System.out.println("配置信息：" + dataModel);
        FileGenerator.doGenerate(dataModel);
        return 0;
    }
}
