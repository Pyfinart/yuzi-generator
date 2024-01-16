package ${basePackage}.generator;

import cn.hutool.core.io.FileUtil;

/**
 * 静态文件生成器
 */
public class StaticGenerator {

    /**
     * copy文件或目录（Hutool实现，会将输入目录完整的copy到输出目录）
     *
     * @param inputPath  输入路径
     * @param outputPath 输出路径
     */
    public static void copyFileByHutool(String inputPath, String outputPath) {
        FileUtil.copy(inputPath, outputPath, false);
    }


}
