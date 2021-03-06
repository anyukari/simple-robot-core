package com.forte.qqrobot.beans.messages.result;

import com.forte.qqrobot.bot.LoginInfo;

import java.util.Objects;

/**
 * 登录信息
 * @see com.forte.qqrobot.bot.LoginInfo
 * @author ForteScarlet <[email]ForteScarlet@163.com>
 * @since JDK1.8
 **/
public class AbstractLoginInfo extends AbstractLoginQQInfo implements LoginInfo {


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractLoginInfo that = (AbstractLoginInfo) o;
        return Objects.equals(getCode(), that.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCode());
    }
}
