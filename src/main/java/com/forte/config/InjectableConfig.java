package com.forte.config;

import java.util.Map;
import java.util.Properties;

/**
 *
 * 可注入的配置接口
 *
 * @author ForteScarlet <[email]ForteScarlet@163.com>
 * @since JDK1.8
 **/
public interface InjectableConfig<T> {

    /**
     * 通过properties注入
     */
    T inject(T config, Properties data);

    /**
     * 通过map注入
     */
    T inject(T config, Map<String, Object> data);

    /**
     * 通过单个键值对儿添加
     */
    T inject(T config, String name, Object value);

}
