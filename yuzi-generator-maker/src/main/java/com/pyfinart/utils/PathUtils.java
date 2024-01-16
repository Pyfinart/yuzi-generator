package com.pyfinart.utils;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PathUtils {
    public static String connectPath(String... paths) {
        if (ArrayUtil.isEmpty(paths)) {
            return "";
        }

        List<String> pathList = Arrays.asList(paths);
        return StrUtil.join(File.separator, pathList);
    }
}
