package com.pyfinart.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
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
import com.pyfinart.maker.template.model.TemplateMakerModelConfig;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FTL模板和meta元信息文件 制作工具，使用字符串替换实现
 */
public class TemplateMaker {

    /**
     * 制作模板和meta文件
     *
     * @param newMeta                  meta文件对应的对象
     * @param templateMakerModelConfig 要挖坑的位置对应的模型，可多个模型，可分组模型
     * @param originalProjectPath      原始文件的路径
     * @param templateMakerFileConfig  原始文件路径下的用来制作模板的文件的路径（相对或绝对）以及过滤条件
     * @param searchStr                用于搜索替换挖坑的内容
     * @param id                       命名空间id，用于区别不同次的生成项目，以及多次对同一生成内容做进一步模板挖坑
     * @return 命名空间id
     */
    private static long makeTemplate(
            Meta newMeta,
            TemplateMakerModelConfig templateMakerModelConfig,
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

        // 处理模型信息
        List<TemplateMakerModelConfig.ModelInfoConfig> models1 = templateMakerModelConfig.getModels();
        // 转换成meta文件接受的ModelDTO
        List<Meta.ModelConfigDTO.ModelDTO> modelDTOList = models1.stream()
                .map(model -> {
                    Meta.ModelConfigDTO.ModelDTO modelConfigDTO = new Meta.ModelConfigDTO.ModelDTO();
                    BeanUtil.copyProperties(model, modelConfigDTO);
                    return modelConfigDTO;
                }).collect(Collectors.toList());

        List<Meta.ModelConfigDTO.ModelDTO> newModelDTOs = new ArrayList<>();
        // 如果有分组，则增加分组配置分组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            String condition = modelGroupConfig.getCondition();
            String groupName = modelGroupConfig.getGroupName();
            String groupKey = modelGroupConfig.getGroupKey();

            // 新增分组配置
            Meta.ModelConfigDTO.ModelDTO groupModelDTO = new Meta.ModelConfigDTO.ModelDTO();
            groupModelDTO.setGroupKey(groupKey);
            groupModelDTO.setGroupName(groupName);
            groupModelDTO.setCondition(condition);
            // model放入一个组内
            groupModelDTO.setModels(modelDTOList);

            newModelDTOs.add(groupModelDTO);
        } else {
            // 不分组，直接添加所有模型信息
            newModelDTOs.addAll(modelDTOList);
        }

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

            // 按照FileInfoConfig中的过滤条件FileFilterConfig过滤
            // 传入绝对路径
            List<File> fileList = FileFilter.doFilter(fileInputPath, fileInfoCOnfig.getFilterConfigList());

            for (File file : fileList) {
                Meta.FileConfigDTO.FileDTO fileDTO = makeFileTemplate(templateMakerModelConfig, file, sourcePath);
                if (fileDTO == null) continue;
                fileDTOS.add(fileDTO);
            }
        }

        String metaOutputPath = sourcePath + File.separator + "meta.json";

        // 三、元信息文件生成

        // 如果有分组，则增加分组配置分组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupName = fileGroupConfig.getGroupName();
            String groupKey = fileGroupConfig.getGroupKey();

            // 新增分组配置
            Meta.FileConfigDTO.FileDTO groupFileDTO = new Meta.FileConfigDTO.FileDTO();
            groupFileDTO.setGroupKey(groupKey);
            groupFileDTO.setGroupName(groupName);
            groupFileDTO.setCondition(condition);
            groupFileDTO.setType(FileTypeEnum.GROUP.getValue());
            // 文件放入一个组内
            groupFileDTO.setFiles(fileDTOS);

            fileDTOS = new ArrayList<>();
            fileDTOS.add(groupFileDTO);
        }

        // 若meta.json存在，说明现在是在追加或者修改元信息
        if (FileUtil.exist(metaOutputPath)) {
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            BeanUtil.copyProperties(newMeta, oldMeta, CopyOptions.create().ignoreNullValue());
            newMeta = oldMeta;

            // 1. 追加配置参数
            List<Meta.FileConfigDTO.FileDTO> files = newMeta.getFileConfig().getFiles();
            files.addAll(fileDTOS);
            List<Meta.ModelConfigDTO.ModelDTO> models = newMeta.getModelConfig().getModels();
            models.addAll(newModelDTOs);

            // 配置驱去重
            newMeta.getModelConfig().setModels(groupDistinct(
                    models,
                    Meta.ModelConfigDTO.ModelDTO::getGroupKey,
                    Meta.ModelConfigDTO.ModelDTO::getModels,
                    Meta.ModelConfigDTO.ModelDTO::getFieldName,
                    Meta.ModelConfigDTO.ModelDTO::setModels
            ));
//            newMeta.getFileConfig().setFiles(fileGroupDistinct(files));
            newMeta.getFileConfig().setFiles(groupDistinct(
                    files,
                    Meta.FileConfigDTO.FileDTO::getGroupKey,
                    Meta.FileConfigDTO.FileDTO::getFiles,
                    Meta.FileConfigDTO.FileDTO::getInputPath,
                    Meta.FileConfigDTO.FileDTO::setFiles
            ));

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
            modelInfoList.addAll(newModelDTOs);

            // 2. 输出元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        }


        return id;
    }

    /**
     * 制作文件模板
     *
     * @param templateMakerModelConfig 要挖坑的位置对应的模型，可多个，可分组
     * @param fileInputFile            原始文件路径下的用来制作模板的文件的绝对路径File对象
     * @param sourcePath               存放制作好的模板代码项目的根路径：/.../yuzi-generator/yuzi-generator-maker/.temp/1749794565135020032/acm-template
     * @return
     */
    private static Meta.FileConfigDTO.FileDTO makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, File fileInputFile, String sourcePath) {

        // 挖坑文件相对路径
        String fileInputPath = fileInputFile.getAbsolutePath().replace(sourcePath + "/", "");
        if (StrUtil.endWith(fileInputPath, ".ftl")) return null;

        String fileOutputPath = fileInputPath + ".ftl";


        // 二、 使用字符串替换生成模板文件
        // 1. 挖坑
        String fileAbsoluteInputPath = fileInputFile.getAbsolutePath();
        String fileAbsoluteOutputPath = fileInputFile.getAbsolutePath() + ".ftl";

        String fileContent;
        // 若存在，则不是第一次生成，在已有基础上做修改
        if (FileUtil.exist(fileAbsoluteOutputPath)) {
            fileContent = FileUtil.readUtf8String(fileAbsoluteOutputPath);
        } else {
            fileContent = FileUtil.readUtf8String(fileAbsoluteInputPath);
        }

        // 多个模型，对于同一个文件，遍历模型进行多轮替换
        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigs = templateMakerModelConfig.getModels();
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        String newFileContent = fileContent;
        String replacement;
        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : modelInfoConfigs) {
            // 无分组
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", modelInfoConfig.getFieldName());
            } else {
                replacement = String.format("${%s.%s}", modelGroupConfig.getGroupKey(), modelInfoConfig.getFieldName());
            }
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }


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
     * 当多次分步制作模版时，需要对同组文件配置进行去重和合并
     *
     * @param files 文件配置列表
     * @return
     */
    private static List<Meta.FileConfigDTO.FileDTO> fileGroupDistinct(List<Meta.FileConfigDTO.FileDTO> files) {
        // 1。将所有文件配置分为有分组和无分组

        // 2。先处理有分组，如下，"groupKey":"a"的两组需要合并和去重
        // filter: {"groupKey":"a", "files":[1, 2]}, {"groupKey":"a", "files":[2, 3]}, {"groupKey":"aa", "files":[4, 5]}
        // groupingBy: {"groupKey":"a", "files":[[1, 2], [2, 3]]}, {"groupKey":"aa", "files":[[4, 5]]}
        Map<String, List<Meta.FileConfigDTO.FileDTO>> fileDTOsMap = files.stream()
                .filter(fileDTO -> StrUtil.isNotBlank(fileDTO.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.FileConfigDTO.FileDTO::getGroupKey)
                );
        // 同组配置合并
        // {"groupKey":"a", "files":[[1, 2], [2, 3]]}
        // {"groupKey":"a", "files":[1, 2, 2, 3]}
        // {"groupKey":"a", "files":[1, 2, 3]}
        Map<String, Meta.FileConfigDTO.FileDTO> resMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.FileConfigDTO.FileDTO>> entry : fileDTOsMap.entrySet()) {
            List<Meta.FileConfigDTO.FileDTO> beforeDistinct = entry.getValue(); // {"groupKey":"a", "files":[[1, 2], [2, 3]]}
            List<Meta.FileConfigDTO.FileDTO> flatBeforeDistinct = beforeDistinct.stream()
                    .flatMap(fileDTO -> fileDTO.getFiles().stream())
                    .collect(Collectors.toList());// {"groupKey":"a", "files":[1, 2, 2, 3]}
            List<Meta.FileConfigDTO.FileDTO> fileDTOsAfterDistinct = distinctBy(
                    flatBeforeDistinct,
                    Meta.FileConfigDTO.FileDTO::getInputPath
            ); // {"groupKey":"a", "files":[1, 2, 3]}

            // 对于多个groupKey相同的组，后来的groupName和condition都可以不一样，使用最新的
            Meta.FileConfigDTO.FileDTO last = CollectionUtil.getLast(beforeDistinct);
            last.setFiles(fileDTOsAfterDistinct);
            resMap.put(entry.getKey(), last);
        }
        // 3。把分组后的结果添加到结果列表
        ArrayList<Meta.FileConfigDTO.FileDTO> mergedList = new ArrayList<>(resMap.values());

        // 4。处理并添加没有分组的文件
        List<Meta.FileConfigDTO.FileDTO> noGroupFileBeforeDistinct = files.stream()
                .filter(fileDTO -> StrUtil.isBlank(fileDTO.getGroupKey()))
                .collect(Collectors.toList());
        mergedList.addAll(distinctBy(noGroupFileBeforeDistinct, Meta.FileConfigDTO.FileDTO::getInputPath));

        return mergedList;
    }

    /**
     * 当多次分步制作模版时，需要对同组模型配置进行去重和合并。逻辑与fileGroupDistinct类似
     *
     * @param models 模型配置列表
     * @return
     */
    private static List<Meta.ModelConfigDTO.ModelDTO> modelGroupDistinct(List<Meta.ModelConfigDTO.ModelDTO> models) {
        // 1。将所有文件配置分为有分组和无分组
        // 2。先处理有分组
        Map<String, List<Meta.ModelConfigDTO.ModelDTO>> modelDTOsMap = models.stream()
                .filter(model -> StrUtil.isNotBlank(model.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfigDTO.ModelDTO::getGroupKey)
                );

        Map<String, Meta.ModelConfigDTO.ModelDTO> resMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.ModelConfigDTO.ModelDTO>> entry : modelDTOsMap.entrySet()) {
            List<Meta.ModelConfigDTO.ModelDTO> beforeDistinct = entry.getValue();
            List<Meta.ModelConfigDTO.ModelDTO> flatBeforeDistinct = beforeDistinct.stream()
                    .flatMap(modelDTO -> modelDTO.getModels().stream())
                    .collect(Collectors.toList());
            List<Meta.ModelConfigDTO.ModelDTO> modelDTOsAfterDistinct = distinctBy(
                    flatBeforeDistinct,
                    Meta.ModelConfigDTO.ModelDTO::getFieldName
            );
            Meta.ModelConfigDTO.ModelDTO last = CollectionUtil.getLast(beforeDistinct);
            last.setModels(modelDTOsAfterDistinct);
            resMap.put(entry.getKey(), last);
        }
        // 3。把分组后的结果添加到结果列表
        ArrayList<Meta.ModelConfigDTO.ModelDTO> mergedList = new ArrayList<>(resMap.values());
        // 4。处理并添加没有分组的文件
        List<Meta.ModelConfigDTO.ModelDTO> noGroupModelBeforeDistinct = models.stream()
                .filter(model -> StrUtil.isBlank(model.getGroupKey()))
                .collect(Collectors.toList());
        mergedList.addAll(distinctBy(noGroupModelBeforeDistinct, Meta.ModelConfigDTO.ModelDTO::getFieldName));
        return mergedList;
    }

    /**
     * 当多次分步制作模版或文件时，需要对同组模型和文件配置进行去重和合并的通用方法
     *
     * @param data              模型配置列表或文件配置列表
     * @param groupKeyGetter    Meta.ModelConfigDTO.ModelDTO::getGroupKey或者Meta.FileConfigDTO.FileDTO::getGroupKey
     * @param itemGetter        Meta.ModelConfigDTO.ModelDTO::getModels或者Meta.FileConfigDTO.FileDTO::getFiles
     * @param distinctKeyGetter Meta.ModelConfigDTO.ModelDTO::getFieldName或Meta.FileConfigDTO.FileDTO::getInputPath
     * @param setItemsMethod    (modelDTO, modelList) -> modelDTO.setModels(modelList)或(fileDTO, fileList) -> fileDTO.setFiles(fileList)
     * @param <T>
     * @return
     */
    private static <T> List<T> groupDistinct(
            List<T> data,
            Function<T, String> groupKeyGetter,
            Function<T, List<T>> itemGetter,
            Function<T, Object> distinctKeyGetter,
            BiConsumer<T, List<T>> setItemsMethod
    ) {
        Map<String, List<T>> configMap = data.stream()
                .filter(element -> StrUtil.isNotBlank(groupKeyGetter.apply(element)))
                .collect(
                        Collectors.groupingBy(groupKeyGetter)
                );

        Map<String, T> resMap = new HashMap<>();
        for (Map.Entry<String, List<T>> entry : configMap.entrySet()) {
            List<T> beforeDistinct = entry.getValue();
            List<T> flatBeforeDistinct = beforeDistinct.stream()
                    .flatMap(element -> itemGetter.apply(element).stream())
                    .collect(Collectors.toList());
            List<T> configsAfterDistinct = distinctBy(flatBeforeDistinct, distinctKeyGetter);
            T last = CollectionUtil.getLast(beforeDistinct);
            setItemsMethod.accept(last, configsAfterDistinct);
            resMap.put(entry.getKey(), last);
        }

        ArrayList<T> mergedList = new ArrayList<>(resMap.values());
        List<T> noGroupItemsBeforeDistinct = data.stream()
                .filter(element -> StrUtil.isBlank(groupKeyGetter.apply(element)))
                .collect(Collectors.toList());
        mergedList.addAll(distinctBy(noGroupItemsBeforeDistinct, distinctKeyGetter));
        return mergedList;
    }


    /**
     * 去重通用方法，在这里用于对模型配置和文件配置去重
     *
     * @param list      要去重的列表
     * @param keyMapper 用于从元素中提取键的函数
     * @param <T>       列表元素的类型目前是Meta.ModelConfigDTO.ModelDTO或Meta.FileConfigDTO.FileDTO
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

//        String searchStr = "Sum: ";

        // 替换变量（第二次）
        String searchStr = "BaseResponse";

        String inputFilePath1 = "src/main/java/com/yupi/springbootinit/common";  // 因为方法中有了sourcePath，文件路径只需要相对路径
        String inputFilePath2 = "src/main/resources/application.yml";

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();

        // 模型组配置
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        modelGroupConfig.setGroupKey("mysql");
        modelGroupConfig.setGroupName("数据库配置");
        templateMakerModelConfig.setModelGroupConfig(modelGroupConfig);

        // 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");

        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig2 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig2.setFieldName("username");
        modelInfoConfig2.setType("String");
        modelInfoConfig2.setDefaultValue("root");
        modelInfoConfig2.setReplaceText("root");

        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig1, modelInfoConfig2);
        templateMakerModelConfig.setModels(modelInfoConfigList);

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

        // 分组配置
        // 分组1
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
        fileGroupConfig.setCondition("output输出");
        fileGroupConfig.setGroupName("testGroup2");
        fileGroupConfig.setGroupKey("测试分组");
        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);
        // 分组2
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(inputFilePath2);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1, fileInfoConfig2));

        long l = makeTemplate(meta, templateMakerModelConfig, originalProjectPath, templateMakerFileConfig, searchStr, 1750752428187742208l);
        System.out.println(l);
    }

}
