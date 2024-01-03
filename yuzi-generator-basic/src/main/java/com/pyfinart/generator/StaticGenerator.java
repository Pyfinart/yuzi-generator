package com.pyfinart.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 静态文件生成器
 */
public class StaticGenerator {

    public static void main(String[] args) {
        String projectRoot = System.getProperty("user.dir");
        // 输入路径
        String inputPath = projectRoot + File.separator + "samples" + File.separator + "acm-template";
        // 输出路径
        String outputPath = projectRoot;

        copyFileByHutool(inputPath, outputPath);
    }

    /**
     * copy文件或目录（Hutool实现，会将输入目录完整的copy到输出目录）
     *
     * @param inputPath  输入路径
     * @param outputPath 输出路径
     */
    public static void copyFileByHutool(String inputPath, String outputPath) {
        FileUtil.copy(inputPath, outputPath, false);
    }

    /**
     * 递归拷贝文件
     *
     * @param inputPath
     * @param outputPath
     */
    public static void copyFileByRecursive(String inputPath, String outputPath) {
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);
        try {
            copyFileByRecursive(inputFile, outputFile);
        } catch (Exception e) {
            System.out.println("复制失败");
            e.printStackTrace();
        }
    }

    /**
     * 文件A => 目录B，则文件A放在目录B下
     * 文件A => 文件B，则文件A覆盖文件B
     * 目录A => 目录B，则目录A放在目录B下
     *
     * @param inputFile
     * @param outputFile
     * @throws IOException
     */
    private static void copyFileByRecursive(File inputFile, File outputFile) throws IOException {
        // 区分是文件还是目录
        if (inputFile.isDirectory()) {
            System.out.println(inputFile.getName());
            File destOutputFile = new File(outputFile, inputFile.getName());
            // 如果是目录，先创建目录
            if (!destOutputFile.exists()) {
                destOutputFile.mkdir();
            }
            // 获取目录下的所有文件和子目录
            File[] files = inputFile.listFiles();
            // 无子文件，直接结束
            if (ArrayUtil.isEmpty(files)) {
                return;
            }
            for (File file : files) {
                copyFileByRecursive(file, destOutputFile);
            }
        } else {
            // 是文件，直接复制到目标目录下
            Path destPath = outputFile.toPath().resolve(inputFile.getName()); // 创建空文件名到输出目录下
            Files.copy(inputFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
