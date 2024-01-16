package ${basePackage}.maker;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import ${basePackage}.maker.cli.CommandExecutor;

import java.io.File;

public class Main {
    public static void main(String[] args) {

//        args = new String[]{"generate", "-l", "-a", "-o"};
//        args = new String[] {"generate", "-a", "-o"};
//        args = new String[]{"config"};
//        args = new String[]{"list"};

        CommandExecutor commandExecutor = new CommandExecutor();
        commandExecutor.doExecute(args);

    }
}