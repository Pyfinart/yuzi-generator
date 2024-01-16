package com.pyfinart.maker.cli.command;

import com.pyfinart.maker.model.DataModel;
import picocli.CommandLine;

import java.lang.reflect.Field;

/**
 * 输出生成器需要配置的用户自定义的参数，GenerateCommand中用到的参数，也即Freemarker的模型类(MainTemplateConfig)的属性
 */
@CommandLine.Command(name = "config", mixinStandardHelpOptions = true)
public class ConfigCommand implements Runnable{


    @Override
    public void run() {
        // 利用反射动态获取配置类的属性
        Class<DataModel> mainTemplateConfigClass = DataModel.class;
        Field[] fields = mainTemplateConfigClass.getDeclaredFields();

//        Field[] fields1 = ReflectUtil.getFields(MainTemplateConfig.class); // 效果与前两行一样
        for (Field field : fields) {
            System.out.println("名称"+field.getName());
            System.out.println("类型"+field.getType());
        }

    }
}
