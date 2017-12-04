package com.ecui.utils;

import java.util.*;

/**
 * 方法
 */
public class Method{
    /**
     * 方法名
     */
    private String name;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 是否构造方法
     */
    private Boolean isConstruction;
    /**
     * 所属控件
     */
    private Control control;
    /**
     * 异常
     */
    private LinkedHashSet<Byte> abnormal = new LinkedHashSet<>();
    /**
     * 简述
     */
    private String brief = "";
    /**
     * 描述
     */
    private String desc = "";
    /**
     * 参数
     */
    private LinkedHashSet<Param> params = new LinkedHashSet<>();
    /**
     * 返回值
     */
    private String result;
    /**
     * 属性
     */
    private String access;
    /**
     * 是否覆盖
     */
    private Boolean isOverride = false;
    /**
     * 方法开始行
     */
    private int startLine;
    /**
     * 方法结束行
     */
    private int endLine;
    /**
     * 是否是事件
     */
    private Boolean isEvent;
    /**
     * 注释
     */
    private List<String> notes = new ArrayList<>();

    public Boolean getEvent() {
        return isEvent;
    }

    public void setEvent(Boolean event) {
        isEvent = event;
    }

    /**
     * 无参构造方法
     */
    public Method() {
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public Method(Control control, Boolean isConstruction, String name, int startLine, int endLine) {
        this.control = control;
        this.isConstruction = isConstruction;
        this.name = name;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getOverride() {
        return isOverride;
    }

    public void setOverride(Boolean override) {
        isOverride = override;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public LinkedHashSet<Param> getParams() {
        return params;
    }

    public void setParams(LinkedHashSet<Param> params) {
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getConstruction() {
        return isConstruction;
    }

    public void setConstruction(Boolean construction) {
        isConstruction = construction;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public LinkedHashSet<Byte> getAbnormal() {
        return abnormal;
    }

    public void setAbnormal(LinkedHashSet<Byte> abnormal) {
        this.abnormal = abnormal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":\"").append(name).append('\"');
        sb.append(",\"brief\":\"").append(brief).append('\"');
        sb.append("}");
        return sb.toString();
    }

    public String toJson(){
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("\"name\":\"").append(name).append('\"');
        sb.append(",\"desc\":\"").append(desc==null?"":desc).append('\"');
        sb.append(",\"params\":").append(params);
        sb.append(",\"result\":\"").append(result==null?"":result).append('\"');
        sb.append(",\"access\":\"").append(access==null?"":access).append('\"');
        sb.append(",\"isOverride\":\"").append(isOverride==null?"":!isOverride?"":isOverride.toString()).append('\"');
        sb.append(",\"fileName\":\"").append(fileName).append('\"');
        sb.append(",\"abnormal\":\"").append(abnormal).append('\"');
        sb.append(",\"isEvent\":\"").append(isEvent==null?"":!isEvent?"":isEvent.toString()).append('\"');
        sb.append(",\"control\":");
        sb.append("{");
        sb.append("\"name\":\"").append(control.getName()).append('\"');
        sb.append(",\"fileName\":\"").append(control.getFileName()).append('\"');
        sb.append(",\"brief\":\"").append(control.getBrief()==null?"":control.getBrief()).append('\"');
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Method method = (Method) o;

        return (name != null ? name.equals(method.name) : method.name == null) && (fileName != null ? fileName.equals(method.fileName) : method.fileName == null) && (notes != null ? notes.equals(method.notes) : method.notes == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (notes != null ? notes.hashCode() : 0);
        return result;
    }

    /**
     * 异常
     */
    public enum Abnormal{
        /**
         * 构造方法中没有call()
         */
        HASNOCALL((byte) 1,"构造方法中没有call()");

        Abnormal(Byte code, String value) {
            this.code = code;
            this.value = value;
        }

        Byte code;
        String value;

        public Byte getCode() {
            return code;
        }

        public void setCode(Byte code) {
            this.code = code;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
