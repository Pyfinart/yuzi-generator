package ${basePackage}.cli.command;


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
        // 输入路径
        String inputPath = "${fileConfig.inputRootPath}"; // 直接从配置文件获取输入项目模板的根路径
        List<File> files = FileUtil.loopFiles(inputPath);
        for (File file : files) {
            System.out.println(file);
        }
    }
}
