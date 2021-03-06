package com.forte.qqrobot.beans.messages.msgget;

import com.forte.qqrobot.beans.messages.FlagAble;
import com.forte.qqrobot.beans.messages.QQCodeAble;

/**
 * 私信消息撤回
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public interface PrivateMsgDelete extends EventGet, QQCodeAble, FlagAble {
    /**
     * 撤回的消息
     * @return 撤回的消息
     */
    @Override
    String getMsg();

    /**
     * 获取此消息的唯一id
     * @return 消息id
     */
    @Override
    String getId();

    /**
     * 获取标识，等同于获取消息id
     * @return 消息id
     */
    @Override
    default String getFlag(){
        return getId();
    }

}
