package com.forte.qqrobot.beans.messages.get;

import com.alibaba.fastjson.annotation.JSONField;
import com.forte.qqrobot.beans.messages.result.GroupHomeworkList;

/**
 * 获取群作业列表
 * @author ForteScarlet <[163邮箱地址]ForteScarlet@163.com>
 * @since JDK1.8
 **/
public interface GetGroupHomeworkList extends InfoGet<GroupHomeworkList> {

    /**
     * 获取通过此类型请求而获取到的参数的返回值的类型
     */
    @Override
    @JSONField(serialize = false)
    default Class<? extends GroupHomeworkList> resultType(){
        return GroupHomeworkList.class;
    }

    String getGroup();

}
