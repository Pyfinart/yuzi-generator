package com.pyfinart.maker.generator;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class ScriptGenerator {

    public static void doGenerator(String outputPath, String jarPath) {
        // linux脚本
        StringBuilder stringBuilder = new StringBuilder();
        // #!/bin/bash
        // java -jar ./target/yuzi-generator-basic-1.0-SNAPSHOT-jar-with-dependencies.jar "$@"
        stringBuilder.append("#!/bin/bash").append("\n");
        stringBuilder.append(String.format("java -jar %s \"$@\"", jarPath)).append("\n");
        FileUtil.writeBytes(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), outputPath);
        // 添加可执行权限，windows执行会报错，姑用try catch
        try {
            Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
            Files.setPosixFilePermissions(Paths.get(outputPath), permissions);
        } catch (IOException e) {

        }

        // windows脚本
        stringBuilder = new StringBuilder();
        // @echo off
        // java -jar ./target/yuzi-generator-basic-1.0-SNAPSHOT-jar-with-dependencies.jar %*
        stringBuilder.append("@echo off").append("\n");
        stringBuilder.append(String.format("java -jar %s %%*", jarPath)).append("\n"); // 需要多打一个%来转义%
        FileUtil.writeBytes(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), outputPath + ".bat");

    }

}
