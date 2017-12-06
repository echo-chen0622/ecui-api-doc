package com.ecui.domain;

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
        sb.append("name:").append(name);
        sb.append(",brief:").append(brief);
        sb.append(",fileName:").append(fileName);
        sb.append(",isEvent:").append(isEvent);
        sb.append("}");
        return sb.toString();
    }

    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("desc",desc);
        map.put("result",result);
        map.put("access",access);
        map.put("isOverride",isOverride);
        map.put("fileName",fileName);
        map.put("isEvent",isEvent);
        map.put("abnormal",abnormal.toArray());
        map.put("params",params.toArray());
        map.put("control",control.toSimpleMap());
        return map;
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

        return (name != null ? name.equals(method.name) : method.name == null) && (fileName != null ? fileName.equals(method.fileName) : method.fileName == null) && (isConstruction != null ? isConstruction.equals(method.isConstruction) : method.isConstruction == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (isConstruction != null ? isConstruction.hashCode() : 0);
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
