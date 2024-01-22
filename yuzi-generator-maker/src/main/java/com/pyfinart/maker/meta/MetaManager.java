package com.pyfinart.maker.meta;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;

public class MetaManager {

    // volatile关键字确保多线程环境下的可见性。即一个线程修改了数据，其他线程可以察觉到修改
    private static volatile Meta meta;

    /**
     * 这是一个单例的meta文件类，因为配置文件在运行过程基本是不变的，这样避免多次重复读取文件，节省很多IO操作的时间
     * @return meta
     */
    public static Meta getMetaObject(){

        if (meta == null) {
            synchronized (MetaManager.class){
                if (meta == null) { // 可能有多个线程同时进入外层的if，所以里面还要加一层if
                    return initMeta();
                }
            }
        }

        return meta;
    }

    private static Meta initMeta(){
        String metaJson = ResourceUtil.readUtf8Str("meta.json");
        meta = JSONUtil.toBean(metaJson, Meta.class);
        // 校验配置参数是否合法，处理默认值
        MetaValidator.doValidateAndFill(meta);
        return meta;
    }

}
