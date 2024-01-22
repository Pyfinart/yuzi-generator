package com.pyfinart.maker;

import com.pyfinart.maker.generator.main.MainGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

/**
 * 生成生成器的程序入口
 */
public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {

//        args = new String[]{"generate", "-l", "-a", "-o"};
//        args = new String[] {"generate", "-a", "-o"};
//        args = new String[]{"config"};
//        args = new String[]{"list"};

//        CommandExecutor commandExecutor = new CommandExecutor();
//        commandExecutor.doExecute(args);

        MainGenerator mainGenerator = new MainGenerator();
        mainGenerator.doGenerate();

    }
}