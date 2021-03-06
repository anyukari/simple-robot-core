package com.forte.qqrobot.beans.messages.result;

/**
 * @see AnonInfo
 * @author ForteScarlet <[email]ForteScarlet@163.com>
 * @since JDK1.8
 **/
public abstract class AbstractAnonInfo extends AbstractInfoResult implements AnonInfo {

    private String id;

    private String anonName;

    private String token;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getAnonName() {
        return anonName;
    }

    public void setAnonName(String anonName) {
        this.anonName = anonName;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String token(){
        return getToken();
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "AnonInfo{" +
                "id='" + getId() + '\'' +
                ", anonName='" + getAnonName() + '\'' +
                ", token='" + token() + '\'' +
                "} " + super.toString();
    }
}
