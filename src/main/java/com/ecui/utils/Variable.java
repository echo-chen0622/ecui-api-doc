package com.ecui.utils;

/**
 * 变量
 */
public class Variable{
    /**
     * 是否重复
     */
    private String repeat;
    /**
     * 变量名
     */
    private String name;
    /**
     * 描述
     */
    private String desc;
    /**
     * 所属控件名
     */
    private Control control;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public Variable(String name) {
        this.name = name;
    }

    /**
     * @param name 变量名
     * @param control 所属控件
     */
    public Variable(String name, Control control) {
        this.name = name;
        this.control = control;
    }

    public Variable(String name,Control control,String desc) {
        this.control = control;
        this.name = name;
        this.desc = desc;
    }

    public Variable() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Variable variable = (Variable) o;

        return name != null ? name.equals(variable.name) : variable.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return  "{\"repeat\":\"" + (repeat ==null?"": repeat) +"\""+
                ",\"name\":\"" + name + "\"" +
                ",\"desc\":\"" + (desc == null?"":desc) + "\"" +
                '}';
    }
}

