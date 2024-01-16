package com.pyfinart.maker.generator;

import java.io.*;

public class JarGenerator {

    public static void doGenerator(String projectDir) throws IOException, InterruptedException {
        // 调用Java自带的Process类执行maven的打包命令
        String otherMavenCommand = "mvn clean package -DskipTests=true";
        String windowsMavenCommand = "mvn.cmd clean package -DskipTests=true";
        String mavenCommand = otherMavenCommand;

        ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand.split(" "));
        processBuilder.directory(new File(projectDir));
        Process process = processBuilder.start();

        // 打印命令执行的输出信息
        InputStream inputStream = process.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor(); // exit code 0正常退出，1异常退出
        System.out.println("命令执行结束，推出码" + exitCode);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        doGenerator("/Users/yhj19/developer/MY_Java/yuzi-generator/yuzi-generator-basic");
    }

}