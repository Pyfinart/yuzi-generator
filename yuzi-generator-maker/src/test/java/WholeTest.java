import cn.hutool.core.util.StrUtil;
import com.pyfinart.utils.PathUtils;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class WholeTest {
    @Test
    public void testPath() {
        String[] paths = {"src", "main", "java"};
        List<String> pathList = Arrays.asList(paths);
//        System.out.println(pathList);
        System.out.println(StrUtil.join(File.separator, pathList));

        System.out.println(PathUtils.connectPath("DataModel.java.ftl"));
    }
}
