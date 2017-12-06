package com.ecui.domain;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author chentiancheng
 * @date 2017/11/22
 * @time 10:24
 * @describe 控件
 */
public class Control {

    /**
     * control or unit
     */
    private String type;
    /**
     * 树节点名称
     */
    private String name;
    /**
     * 父节点名称
     */
    private String parentName;
    /**
     * 父节点
     */
    private Control parentNode;
    /**
     * 子节点集合
     */
    private List<Control> children = new ArrayList<>();
    /**
     * 变量集合
     */
    private LinkedHashSet<Variable> variables = new LinkedHashSet<>();
    /**
     * 控件开始行
     */
    private int startLine;
    /**
     * 控件结束行
     */
    private int endLine;
    /**
     * 生成文件名
     */
    private String fileName;
    /**
     * 所属文件
     */
    private String pathFrom;
    /**
     * 注释
     */
    private List<String> notes = new ArrayList<>();
    /**
     * 简述
     */
    private String brief = "";
    /**
     * 详细描述
     */
    private String desc = "";
    /**
     * 属性
     */
    private String access;
    /**
     * 示例
     */
    private String example;
    /**
     * option的属性
     */
    private LinkedHashSet<Param> optionParams = new LinkedHashSet<>();
    /**
     * 方法集合
     */
    private LinkedHashSet<Method> methods = new LinkedHashSet<>();
    /**
     * 样式
     */
    private String style;

    // getter and setter start
    public void setVariables(LinkedHashSet<Variable> variables) {
        this.variables = variables;
    }

    public LinkedHashSet<Param> getOptionParams() {
        return optionParams;
    }

    public void setOptionParams(LinkedHashSet<Param> optionParams) {
        this.optionParams = optionParams;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Control getParentNode() {
        return parentNode;
    }

    public void setParentNode(Control parentNode) {
        this.parentNode = parentNode;
    }

    public List<Control> getChildren() {
        return children;
    }

    public void setChildren(List<Control> children) {
        this.children = children;
    }

    public LinkedHashSet<Variable> getVariables() {
        return variables;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPathFrom() {
        return pathFrom;
    }

    public void setPathFrom(String pathFrom) {
        this.pathFrom = pathFrom;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public LinkedHashSet<Method> getMethods() {
        return methods;
    }

    public void setMethods(LinkedHashSet<Method> methods) {
        this.methods = methods;
    }
    //getter and setter end

    /**
     * 无参构造方法
     */
    public Control() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"name\":\"").append(name).append('\"');
        sb.append(",\"parent\":\"").append(parentName).append('\"');
        sb.append(",\"brief\":\"").append(brief).append('\"');
        sb.append(",\"children\":").append(children);
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

        Control control = (Control) o;

        return (name != null ? name.equals(control.name) : control.name == null) && (fileName != null ? fileName.equals(control.fileName) : control.fileName == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }

    /**
     * 根据节点map,生成树,返回根节点
     * @param controlMap 节点集合
     * @return 根节点集合
     */
    public static List<Control> getTree(Map<String,Control> controlMap){
        List<Control> treeRoot = new ArrayList<>();
        controlMap.forEach((s, control) -> {
            if (control.getParentName()!=null) {
                Control parentNode = controlMap.get(control.getParentName());
                if (parentNode == null){
                    System.out.println(control);
                }
                control.setParentNode(parentNode);
                assert parentNode != null;
                parentNode.getChildren().add(control);
            }else {
                treeRoot.add(control);
            }
        });
        return treeRoot;
    }

    public Map<String,Object> toSimpleMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("brief",brief);
        map.put("fileName",fileName);
        return map;
    }

    public Map<String, Object> toMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("fileName",fileName);
        map.put("pathFrom",pathFrom);
        map.put("brief",brief);
        map.put("desc",desc);
        map.put("parent",parentName);
        map.put("access",access);
        map.put("type",type);
        map.put("style",style);
        map.put("example",example);
        //子控件集合
        LinkedList<Map<String,Object>> childList = new LinkedList<>();
        children.forEach(child -> childList.add(child.toSimpleMap()));
        map.put("children",childList);
        //option支持的属性
        map.put("optionParams",optionParams.toArray());
        //方法集合
        map.put("methods",methods.toArray());
        //变量集合
        map.put("variables",variables.toArray());
        return map;
    }

}