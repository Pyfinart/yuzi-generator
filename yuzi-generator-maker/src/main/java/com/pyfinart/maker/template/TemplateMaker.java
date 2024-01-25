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
import com.pyfinart.maker.template.enums.FileFilterRangeEnum;
import com.pyfinart.maker.template.enums.FileFilterRuleEnum;
import com.pyfinart.maker.template.model.FileFilterConfig;
import com.pyfinart.maker.template.model.TemplateMakerFileConfig;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FTL模板和meta元信息文件 制作工具，使用字符串替换实现
 */
public class TemplateMaker {

    /**
     * 制作模板和meta文件
     *
     * @param newMeta                 meta文件对应的对象
     * @param modelDTO                要挖坑的位置对应的模型
     * @param originalProjectPath     原始文件的路径
     * @param templateMakerFileConfig 原始文件路径下的用来制作模板的文件的路径（相对或绝对）以及过滤条件
     * @param searchStr               用于搜索替换挖坑的内容
     * @param id                      命名空间id，用于区别不同次的生成项目，以及多次对同一生成内容做进一步模板挖坑
     * @return 命名空间id
     */
    private static long makeTemplate(
            Meta newMeta,
            Meta.ModelConfigDTO.ModelDTO modelDTO,
            String originalProjectPath,
            TemplateMakerFileConfig templateMakerFileConfig,
            String searchStr,
            Long id) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }


        // 指定原始项目路径
        String projectPath = System.getProperty("user.dir");

        // 复制目录
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id; // /.../yuzi-generator/yuzi-generator-maker/.temp/1749794565135020032

        // 目录不存在，则为首次制作
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originalProjectPath, templatePath, true); //将原始代码复制一份
        }

        // 一。输入信息

        // 2. 输入文件的信息
        // /.../yuzi-generator/yuzi-generator-maker/.temp/1749794565135020032/acm-template
        String sourcePath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originalProjectPath)).toString();

        // 二、模板文件生成
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigs = templateMakerFileConfig.getFiles();
        ArrayList<Meta.FileConfigDTO.FileDTO> fileDTOS = new ArrayList<>();
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoCOnfig : fileInfoConfigs) {
            String fileInputPath = fileInfoCOnfig.getPath();

            // 如果填的是相对路径，要改为绝对路径
            if (!fileInputPath.startsWith(sourcePath)) {
                fileInputPath = sourcePath + File.separator + fileInputPath;
            }

            // 传入绝对路径
            List<File> fileList = FileFilter.doFilter(fileInputPath, fileInfoCOnfig.getFilterConfigList());

            for (File file : fileList) {
                Meta.FileConfigDTO.FileDTO fileDTO = makeFileTemplate(modelDTO, file, searchStr, sourcePath);
                if (fileDTO == null) continue;
                fileDTOS.add(fileDTO);
            }
        }

        String metaOutputPath = sourcePath + File.separator + "meta.json";

        // 三、元信息文件生成
        // 若meta.json存在，说明现在是在追加或者修改元信息
        if (FileUtil.exist(metaOutputPath)) {
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            BeanUtil.copyProperties(newMeta, oldMeta, CopyOptions.create().ignoreNullValue());
            newMeta = oldMeta;

            // 1. 追加配置参数
            List<Meta.FileConfigDTO.FileDTO> files = newMeta.getFileConfig().getFiles();
            files.addAll(fileDTOS);
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

            fileDTOList.addAll(fileDTOS);

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
     * 制作文件模板
     *
     * @param modelDTO      要挖坑的位置对应的模型
     * @param fileInputFile 原始文件路径下的用来制作模板的文件的绝对路径File对象
     * @param searchStr     用于搜索替换挖坑的内容
     * @param sourcePath    存放制作好的模板代码项目的根路径：/.../yuzi-generator/yuzi-generator-maker/.temp/1749794565135020032/acm-template
     * @return
     */
    private static Meta.FileConfigDTO.FileDTO makeFileTemplate(Meta.ModelConfigDTO.ModelDTO modelDTO, File fileInputFile, String searchStr, String sourcePath) {

        // 挖坑文件相对路径
        String fileInputPath = fileInputFile.getAbsolutePath().replace(sourcePath + "/", "");
        if (StrUtil.endWith(fileInputPath, ".ftl")) return null;

        String fileOutputPath = fileInputPath + ".ftl";


        // 二、 使用字符串替换生成模板文件
        // 1. 挖坑
        String fileAbsoluteInputPath = fileInputFile.getAbsolutePath();
        String fileAbsoluteOutputPath = fileInputFile.getAbsolutePath() + ".ftl";

        // 若存在，则不是第一次生成，在已有基础上做修改
        String fileContent;
        if (FileUtil.exist(fileAbsoluteOutputPath)) {
            fileContent = FileUtil.readUtf8String(fileAbsoluteOutputPath);
        } else {
            fileContent = FileUtil.readUtf8String(fileAbsoluteInputPath);
        }

        String replacement = String.format("${%s}", modelDTO.getFieldName());
        String newFileContent = StrUtil.replace(fileContent, searchStr, replacement);


        // 三、生成元信息配置文件
        // 实现思路，首先在程序中实例化Meta类，并填充参数，在写入文件

        Meta.FileConfigDTO.FileDTO fileDTO = new Meta.FileConfigDTO.FileDTO();
        fileDTO.setInputPath(fileInputPath);
        fileDTO.setOutputPath(fileOutputPath);
        fileDTO.setType(FileTypeEnum.FILE.getValue());

        // 写入模板文件
        // 要先判断改文件是否真的有挖坑，没有说明是静态生成，不需要ftl模板
        if (newFileContent.equals(fileContent)) {
            if (FileUtil.exist(fileAbsoluteOutputPath)) {
                fileDTO.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
            } else {
                // 输出路径 = 输入路径
                fileDTO.setOutputPath(fileInputPath);
                fileDTO.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            }

        } else {
            fileDTO.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
            FileUtil.writeUtf8String(newFileContent, fileAbsoluteOutputPath);
        }

        return fileDTO;
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
        String originalProjectPath = new File(projectPath).getParent() + File.separator + "samples/springboot-init";
        String fileInputPath = "src/main/java/com/yupi/springbootinit"; // 因为方法中有了sourcePath，文件路径只需要相对路径

//        String searchStr = "Sum: ";

        // 替换变量（第二次）
        String searchStr = "BaseResponse";

        String inputFilePath1 = "src/main/java/com/yupi/springbootinit/common";
        String inputFilePath2 = "src/main/java/com/yupi/springbootinit/controller";
        List<String> inputFilePathList = Arrays.asList(inputFilePath1, inputFilePath2);

        // 文件过滤
        // 只处理 common 包下文件名称包含 Base 的文件和 controller 包下的文件。
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(inputFilePath1);
        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
        FileFilterConfig fileFilterConfig = FileFilterConfig.builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .value("Base")
                .build();
        fileFilterConfigList.add(fileFilterConfig);
        fileInfoConfig1.setFilterConfigList(fileFilterConfigList);

        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(inputFilePath2);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1, fileInfoConfig2));

        long l = makeTemplate(meta, modelDTO, originalProjectPath, templateMakerFileConfig, searchStr, null);
        System.out.println(l);
    }

}
