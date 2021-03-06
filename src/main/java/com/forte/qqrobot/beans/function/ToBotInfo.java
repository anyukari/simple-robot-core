package com.forte.qqrobot.beans.function;

import com.forte.qqrobot.bot.BotInfo;

import java.util.function.BiFunction;

/**
 * 将数据转化为ToBotInfo的工具性质函数，提供一个默认的impl对象。
 * 其转化类型为数据封装，并不用于验证后的botInfo
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public interface ToBotInfo {

    BotInfo toBotInfo(String code, String ip, int port, String path);
    BotInfo toBotInfo(String code, String urlPath);


    default ToBotInfo getInstance(ExFunction<String, Integer, String, String> toFullPath, BiFunction<String, String, BotInfo> toBot){
        return new ToBotInfoImpl(toFullPath, toBot);
    }

}
