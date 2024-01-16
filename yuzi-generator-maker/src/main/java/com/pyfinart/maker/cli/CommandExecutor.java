package com.pyfinart.maker.cli;

import com.pyfinart.maker.cli.command.ConfigCommand;
import com.pyfinart.maker.cli.command.GenerateCommand;
import com.pyfinart.maker.cli.command.ListCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * 命令模式的调用者，相当于遥控器，里面有很多命令，相当于遥控器的按钮
 */
@Command(name = "yuzi", mixinStandardHelpOptions = true)
public class CommandExecutor implements Runnable {

    private final CommandLine commandLine;

    // 初始化命令子命令
    {
        commandLine = new CommandLine(this)
                .addSubcommand(new GenerateCommand())
                .addSubcommand(new ConfigCommand())
                .addSubcommand(new ListCommand());
    }


    /**
     * 不输入子命令时执行，但是调用者本身没有自己的命令，提示用户输入子命令
     */
    @Override
    public void run() {
        // 不输入子命令时，给出友好提示
        System.out.println("请输入具体命令，或者输入 --help 查看命令提示");
    }

    /**
     * 执行命令
     *
     * @param args
     * @return
     */
    public Integer doExecute(String[] args) {
        return commandLine.execute(args);
    }
}
