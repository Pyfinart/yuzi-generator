package com.pyfinart.cli.example;

import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

public class Login implements Callable<Integer> { // 交互式输入只能实现Callable，泛型是退出码
    @Option(names = {"-u", "--user"}, description = "User name")
    String user;

    @Option(names = {"-p", "--password"}, description = "Passphrase", interactive = true, prompt = "请输入密码: ",
            arity = "0..1") // arity="0..1"：在命令行中接收0到1个参数是，若接收到1个则不触发交互，如果接收到0个，则触发交互，提示用户输入
    String password;

    @Option(names = {"-cp", "--checkPassword"}, description = "Check Password", interactive = true, prompt = "确认密码: ",
            arity = "0..1")
    String cp;

    public Integer call() throws Exception {
        System.out.println("password = " + password);
        System.out.println("checkPassword = " + cp);
        return 0;
    }

    public static void main(String[] args) {
//        new CommandLine(new Login()).execute("-u", "user123", "-p", "sfa", "-cp");
//        new CommandLine(new Login()).execute("-u", "user123", "-p", "-cp");
        new CommandLine(new Login()).execute("-u", "user123", "-p");

    }
}