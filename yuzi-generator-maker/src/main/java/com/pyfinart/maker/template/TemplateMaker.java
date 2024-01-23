package com.pyfinart.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.pyfinart.maker.meta.Meta;
import com.pyfinart.maker.meta.enums.FileGenerateTypeEnum;
import com.pyfinart.maker.meta.enums.FileTypeEnum;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FTL模板和meta元信息文件 制作工具，使用字符串替换实现
 */
public class TemplateMaker {

    private static long makeTemplate(Meta newMeta, Meta.ModelConfigDTO.ModelDTO modelDTO, String originalProjectPath, String fileInputPath, String searchStr, Long id) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }


        // 指定原始项目路径
        String projectPath = System.getProperty("user.dir");

        // 复制目录
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id;

        // 目录不存在，则为首次制作
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originalProjectPath, templatePath, true); //将原始代码复制一份
        }

        // 一。输入信息

        // 2. 输入文件的信息
        String sourcePath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originalProjectPath)).toString();

        String fileOutputPath = fileInputPath + ".ftl";


        // 二、 使用字符串替换生成模板文件
        // 1. 挖坑
        String fileAbsoluteInputPath = sourcePath + File.separator + fileInputPath;
        String fileAbsoluteOutputPath = sourcePath + File.separator + fileOutputPath;

        // 若存在，则不是第一次生成，在已有基础上做修改
        String fileContent;
        if (FileUtil.exist(fileAbsoluteOutputPath)) {
            fileContent = FileUtil.readUtf8String(fileAbsoluteOutputPath);
        } else {
            fileContent = FileUtil.readUtf8String(fileAbsoluteInputPath);
        }

        String replacement = String.format("${%s}", modelDTO.getFieldName());
        String newFileContent = StrUtil.replace(fileContent, searchStr, replacement);

        // 2. 写入模板文件
        FileUtil.writeUtf8String(newFileContent, fileAbsoluteOutputPath);

        // 三、生成元信息配置文件
        // 实现思路，首先在程序中实例化Meta类，并填充参数，在写入文件

        Meta.FileConfigDTO.FileDTO fileDTO = new Meta.FileConfigDTO.FileDTO();
        fileDTO.setInputPath(fileInputPath);
        fileDTO.setOutputPath(fileOutputPath);
        fileDTO.setType(FileTypeEnum.FILE.getValue());
        fileDTO.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        String metaOutputPath = sourcePath + File.separator + "meta.json";

        // 若meta.json存在，说明现在是在追加或者修改元信息
        if (FileUtil.exist(metaOutputPath)) {
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            BeanUtil.copyProperties(newMeta, oldMeta, CopyOptions.create().ignoreNullValue());
            newMeta = oldMeta;

            // 1. 追加配置参数
            List<Meta.FileConfigDTO.FileDTO> files = newMeta.getFileConfig().getFiles();
            files.add(fileDTO);
            List<Meta.ModelConfigDTO.ModelDTO> models = newMeta.getModelConfig().getModels();
            models.add(modelDTO);

            // 配置驱去重
            newMeta.getModelConfig().setModels(distinctBy(models, Meta.ModelConfigDTO.ModelDTO::getFieldName));
            newMeta.getFileConfig().setFiles(distinctBy(files, Meta.FileConfigDTO.FileDTO::getInputPath));

            // 2. 写回
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        } else {
            // 1. 构造配置参数
            Meta.FileConfigDTO fileConfigDTO = new Meta.FileConfigDTO();
            newMeta.setFileConfig(fileConfigDTO);
            fileConfigDTO.setSourceRootPath(sourcePath);

            List<Meta.FileConfigDTO.FileDTO> fileDTOList = new ArrayList<>();
            fileConfigDTO.setFiles(fileDTOList);

            fileDTOList.add(fileDTO);

            Meta.ModelConfigDTO modelConfig = new Meta.ModelConfigDTO();
            newMeta.setModelConfig(modelConfig);

            List<Meta.ModelConfigDTO.ModelDTO> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.add(modelDTO);

            // 2. 输出元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        }


        return id;
    }

    /**
     * 去重通用方法，在这里用于对模型配置和文件配置去重
     *
     * @param list      要去重的列表
     * @param keyMapper 用于从元素中提取键的函数
     * @param <T>       列表元素的类型
     * @return 去重后的列表
     */
    private static <T> List<T> distinctBy(List<T> list, Function<? super T, ?> keyMapper) {
        return new ArrayList<>(
                list.stream()
                        .collect(Collectors.toMap(keyMapper, Function.identity(), (e, r) -> r))
                        .values()
        );
    }
    // 从性能的角度来看，Function.identity() 优于 o -> o。虽然这种性能差异在大多数情况下可能微不足道，但在某些高性能场景中可能变得重要。
    // Function.identity() 返回的是一个单例恒等函数，这意味着它不会每次调用时都创建一个新的对象。相反，o -> o 每次使用时都会创建一个新的
    // Function 实例。因此，使用 Function.identity() 可以减少不必要的对象创建，从而减少垃圾回收的压力。

    public static void main(String[] args) {

        String name = "acm-template-generator";
        String description = "ACM 示例模板生成器";
        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);

        // 3. 模型参数的信息(这里是MainTemplate的挖坑参数)
//        Meta.ModelConfigDTO.ModelDTO modelDTO = new Meta.ModelConfigDTO.ModelDTO();
//        modelDTO.setFieldName("output");
//        modelDTO.setType("String");
//        modelDTO.setDefaultValue("Sum: ");

        // 模型参数信息（第二次）
        Meta.ModelConfigDTO.ModelDTO modelDTO = new Meta.ModelConfigDTO.ModelDTO();
        modelDTO.setFieldName("className");
        modelDTO.setType("String");

        String projectPath = System.getProperty("user.dir");
        String originalProjectPath = new File(projectPath).getParent() + File.separator + "samples/acm-template";
        String fileInputPath = "src/com/yupi/acm/MainTemplate.java"; // 因为方法中有了sourcePath，文件路径只需要相对路径

//        String searchStr = "Sum: ";

        // 替换变量（第二次）
        String searchStr = "MainTemplate";

        long l = makeTemplate(meta, modelDTO, originalProjectPath, fileInputPath, searchStr, 1749794565135020032l);
        System.out.println(l);
    }

}
