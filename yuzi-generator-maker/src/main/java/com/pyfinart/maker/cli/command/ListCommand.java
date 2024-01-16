package com.pyfinart.maker.cli.command;


import cn.hutool.core.io.FileUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

/**
 * 打印用于生成的目录里面的所有文件
 */
@CommandLine.Command(name = "list", mixinStandardHelpOptions = true)
public class ListCommand implements Runnable{
    @Override
    public void run() {
        String projectPath = System.getProperty("user.dir");

        // 项目根路径
        File parentFile = new File(projectPath).getParentFile();

        // 输入路径
        String inputPath = new File(parentFile, "samples/acm-template").getParentFile().getAbsolutePath();
        List<File> files = FileUtil.loopFiles(inputPath);
        for (File file : files) {
            System.out.println(file);
        }
    }
}
