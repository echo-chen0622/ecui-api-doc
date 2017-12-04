package com.ecui.utils;

/**
 * 参数
 */
public class Param{
    //名称
    private String name;
    //描述
    private String desc;

    public Param() {
    }

    public Param(String name) {
        this.name = name;
    }

    public Param(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return  "{\"name\":\"" + name + "\""+
                ", \"desc\":\""+ (desc==null?"":desc) + "\"" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Param param = (Param) o;

        return name != null ? name.equals(param.name) : param.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
