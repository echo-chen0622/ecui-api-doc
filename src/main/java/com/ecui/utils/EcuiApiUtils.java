package com.ecui.utils;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ecui.utils.FileUtils.getEndLine;
import static com.ecui.utils.FileUtils.getStartNoteLine;


/**
 * Created with IntelliJ IDEA.
 *
 * @author chentiancheng
 * @date 2017/11/21
 * @time 16:48
 * @describe
 */
public class EcuiApiUtils {

    /**
     * 当前文件控件集合
     */
    private LinkedList<Control> controlList;
    /**
     * 当前文件方法集合
     */
    private LinkedList<Method> methodList;
    /**
     * 构造方法及其子方法集合
     */
    private LinkedList<Method> constructionMethodList;
    /**
     * 控件集合
     */
    private Map<String,Control> controlMap = new HashMap<>();
    /**
     * 方法集合
     */
    private Map<String,Method> methodMap = new HashMap<>();
    /**
     * 文件的行列表
     */
    private List<String> lineList;
    /**
     * 当前方法
     */
    private Method method;
    /**
     * 当前控件
     */
    private Control control;

    /**
     * '_'开头的变量
     */
    private static Pattern VARIABLE = Pattern.compile("(?<=this\\.)_[a-zA-Z]+");
    /**
     * 地址
     */
    private static Pattern PATH = Pattern.compile("\\\\src\\\\.+");
    /**
     * 控件名
     */
    private static Pattern CONTROLNAME = Pattern.compile("(?<=ui\\.)[a-zA-Z.]+");
    /**
     * 控件内控件名
     */
    private static Pattern CHILDCONTROLNAME = Pattern.compile("^[a-zA-Z.]+");
    /**
     * 构造方法
     */
    private static Pattern CONSTRUCTIONMETHOD = Pattern.compile("^function *\\(el, options\\) *\\{");
    /**
     * 普通方法
     */
    private static Pattern METHOD = Pattern.compile("^\\$*[a-zA-Z]+(?= *: *[a-zA-Z]+)");
    /**
     * 参数,需要先去空格化
     */
    private static Pattern PARAM = Pattern.compile("(?<=(function\\()|,)[a-zA-Z]+(?=[),])");
    /**
     * 文件头注释中,'_'开头的变量
     */
    private static Pattern VARIABLENAME = Pattern.compile("(^_[a-zA-Z]+)|(^\\$\\$[a-zA-Z]+)");
    /**
     * options 属性
     */
    private static Pattern OPTIONS = Pattern.compile("(^[a-zA-Z]+)");

    public Map<String, Control> getControlMap() {
        return controlMap;
    }

    public void setControlMap(Map<String, Control> controlMap) {
        this.controlMap = controlMap;
    }

    public Map<String, Method> getMethodMap() {
        return methodMap;
    }

    public void setMethodMap(Map<String, Method> methodMap) {
        this.methodMap = methodMap;
    }

    public List<String> getLineList() {
        return lineList;
    }

    public void setLineList(List<String> lineList) {
        this.lineList = lineList;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    /**
     * 获取树的各节点
     * @param fileSet
     * @return
     */
    public void scanFile(Set<File> fileSet) {

        //循环遍历所有文件
        fileSet.forEach((File file) -> {
            //文件地址
            String path = file.getPath();
            Matcher pathMatcher = PATH.matcher(path);
            if (pathMatcher.find()){
                path = pathMatcher.group().replaceAll("\\\\","\\\\\\\\");
            }
            path = "..."+path;
            //将文件转换成行列表
            lineList = FileUtils.fileToLineList(file);
            //控件及其子控件集合
            controlList = new LinkedList<>();
            //方法集合
            methodList = new LinkedList<>();
            //构造方法及其子方法集合
            constructionMethodList = new LinkedList<>();

            for (int i = 0; i < lineList.size(); i++) {
                //当前所属控件（可能为空，意味着不在任何控件内
                control = getControl(controlList, i);
                //当前所属方法(可能为空
                method = getMethod(methodList, i);
                String line = lineList.get(i).trim();
                if (line.contains("core.inherits(")) {
                    //发现新的控件继承关系
                    //读取父控件
                    String parentNode = lineList.get(i + 1).trim().substring(0, lineList.get(i + 1).trim().lastIndexOf(",")).trim();
                    //拼装方法内控件名
                    String node;
                    if (line.contains(":")) {
                        //控件内
                        Matcher childControlMatcher = CHILDCONTROLNAME.matcher(line);
                        childControlMatcher.find();
                        node = childControlMatcher.group();
                        if (parentNode.contains("prototype.")) {
                            node = "prototype." + node;
                            parentNode = parentNode.replaceFirst("prototype.", "");
                        }
                        if (control != null) {
                            node = control.getName() + "." + node;
                        } else {
                            //获得当前文件名（去后缀，首字母大写，将-{} 转换成大写
                            String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
                            //将-X 转换成大写X
                            fileName = joiner2Camel(fileName, '-');
                            node = fileName + "." + node;
                        }
                        //生成控件
                        getNewControl(node,parentNode,path,i);
                    } else {
                        //主控件
                        if (line.startsWith("var")) {
                            //内部私有控件，跳过扫描
                            i = getEndLine(lineList,i,'(',')');
                            continue;
                        }
                        Matcher controlNameMatcher = CONTROLNAME.matcher(line);
                        controlNameMatcher.find();
                        node = controlNameMatcher.group();
                        //生成方法
                        getNewControl(node,parentNode,path,i);
                        //扫描头部注释
                        scanFileHeader();
                    }
                }else if(control != null) {
                    //控件内代码
                    //正则匹配
                    Matcher variableMatcher = VARIABLE.matcher(line);
                    Matcher methodMatcher = METHOD.matcher(line);
                    Matcher constructionMethodMatcher = CONSTRUCTIONMETHOD.matcher(line);
                    //找'_'开头的变量
                    while (variableMatcher.find()) {
                        Variable variable = new Variable(variableMatcher.group(),control);
                        if (!control.getVariables().contains(variable)){
                            //没有找到同名变量则新增
                            control.getVariables().add(variable);
                        }
                    }
                    //匹配是否为方法
                    if (constructionMethodMatcher.find()) {
                        //构造方法
                        newMethod("constructionMethod",true,constructionMethodList,i);
                    }else if (methodMatcher.find()){
                        //普通方法
                        if (method!=null&&method.getConstruction()){
                            //在构造方法内
                            newMethod(methodMatcher.group(),true,constructionMethodList,i);
                        }else {
                            //不在构造方法内
                            if (method==null) {
                                newMethod(methodMatcher.group(), false, methodList, i);
                                //分析注释
                                anlyzeNotesForMethod();
                            }
                        }
                    }else if (method!=null){
                        //方法内
                        if (method.getConstruction()&& "constructionMethod".equals(method.getName())) {
                            //构造方法内
                            if (line.replaceAll(" ","").contains(".call(this,el,options)")){
                                //包含.call()
                                method.getAbnormal().remove(Method.Abnormal.HASNOCALL.getCode());
                            }
                        }
                    }
                }
            }
        });
    }

    public void getNewControl(String node,String parentNode,String path,int i){
        //设置根节点的父节点为空
        if ("null".equals(parentNode)) {
            parentNode = null;
        } else {
            //去掉开头的"ui."
            parentNode = parentNode.substring(3);
        }
        //集中处理控件节点
        control = new Control();
        control.setName(node);
        control.setParentName(parentNode);
        control.setStartLine(i);
        control.setEndLine(getEndLine(lineList, i, '(', ')'));
        control.setFileName(node.replace('.','-'));
        control.setPathFrom(path);
        //处理紧挨控件上面的注释
        anlyzeNotesForControl();
        controlList.add(control);
        controlMap.put(node,control);
    }

    /**
     * 扫描头部注释
     * @return
     */
    private void scanFileHeader() {
        String example = "";
        int endLineNum = getEndLine(lineList,0,"/*","*/");
        for (int i = 1; i < endLineNum-1; i++) {
            String line = lineList.get(i);
            //对不是"属性","示例"的行操作
            if (line.startsWith("@")) {
                System.out.println("注释中找到未知语法;文件:" + control.getPathFrom() + ",行数:" + i + ",语法:" + line);
            }else if (!"属性".equals(line.trim())&&!"示例".equals(line.trim())){
                //判断是否是变量的描述
                Matcher variableNameMatcher = VARIABLENAME.matcher(line);
                if (variableNameMatcher.find()){
                    //变量名及描述
                    String variableName = variableNameMatcher.group();
                    String variableDesc = line.replace(variableName,"").trim();
                    if (variableDesc.startsWith("-")){
                        //去掉'-'
                        variableDesc = variableDesc.substring(1,variableDesc.length()).trim();
                    }
                    control.getVariables().add(new Variable(variableName,control,variableDesc));
                }else {
                    //存入控件的详细描述
                    String html = escape(line);
                    example = example.concat(html+"</br>");
                }
            }
        }
        control.setExample(example);
    }

    /**
     * 转义
     * @param html
     * @return
     */
    private String escape(String html) {
        return  html.replaceAll("\"","&quot;")
                .replaceAll("<","&lt;")
                .replaceAll(">","&gt;")
                .replaceAll(" ","&nbsp;");
    }

    /**
     * 解析控件的注释
     */
    private void anlyzeNotesForControl() {
        int notesStartLine = getStartNoteLine(lineList,control.getStartLine()-1,"*/","/**");
        if (notesStartLine == -1){
            return;
        }
        //解析注释
        for (int i = notesStartLine+1; i < control.getStartLine()-1; i++) {
            String line = lineList.get(i).trim();
            control.getNotes().add(line);
            //去掉开头的'*'
            line = line.substring(1).trim();

            if (line.startsWith("@")){
                if (startWithIgnoreCase(line,"@public")){
                    control.setAccess("public");
                }else if (startWithIgnoreCase(line,"@protected")){
                    control.setAccess("protected");
                }else if (startWithIgnoreCase(line,"@private")){
                    control.setAccess("private");
                }else if (startWithIgnoreCase(line,"@control")){
                    control.setType("control");
                    control.setStyle(lineList.get(control.getStartLine()+2).trim().substring(1,lineList.get(control.getStartLine()+2).trim().length()-2));
                }else if (startWithIgnoreCase(line,"@unit")){
                    control.setType("unit");
                }else {
                    System.out.println("注释中找到未知语法;文件:"+control.getPathFrom()+",行数:"+i+",语法:"+line);
                }
            }else if (!"".equals(line)&&!"options 属性：".equals(line)){
                Matcher optionsMather = OPTIONS.matcher(line);
                if (optionsMather.find()){
                    String paramName = optionsMather.group();
                    String paramDesc = line.replace(paramName,"").trim();
                    control.getOptionParams().add(new Param(paramName,paramDesc));
                }else {
                    String html = escape(line);
                    if (!"".equals(control.getBrief())) {
                        //有简介
                        control.setDesc(control.getDesc().concat(html) + "</br>");
                    } else {
                        //没简介（中文名
                        control.setBrief(html);
                    }
                }
            }
        }
    }

    /**
     * 解析方法的注释
     */
    private void anlyzeNotesForMethod() {
        int notesStartLine = getStartNoteLine(lineList,method.getStartLine()-1,"*/","/**");
        if (notesStartLine == -1){
            return;
        }
        //解析注释
        for (int i = notesStartLine+1; i < method.getStartLine()-1; i++) {
            String line = lineList.get(i).trim();
            method.getNotes().add(line);
            line = line.substring(1).trim();
            if (line.startsWith("@")){
                if (startWithIgnoreCase(line,"@param")){
                    String lineForParam = line.substring(6).trim();
                    method.getParams().forEach(param -> {
                        if (lineForParam.contains(param.getName())){
                            param.setDesc(lineForParam);
                        }
                    });
                }else if (startWithIgnoreCase(line,"@override")){
                    method.setOverride(true);
                }else if (startWithIgnoreCase(line,"@return")){
                    method.setResult(line.substring(7).trim());
                }else if (startWithIgnoreCase(line,"@public")){
                    method.setAccess("public");
                }else if (startWithIgnoreCase(line,"@protected")){
                    method.setAccess("protected");
                }else if (startWithIgnoreCase(line,"@private")){
                    method.setAccess("private");
                }else if (startWithIgnoreCase(line,"@event")) {
                    method.setEvent(true);
                }else {
                    System.out.println("注释中找到未知语法;文件:"+method.getControl().getPathFrom()+",行数:"+i+",语法:"+line+"");
                }
            }else if (!"".equals(line)){
                String html = escape(line);
                if (!"".equals(method.getBrief())){
                    //有简介
                    method.setDesc(method.getDesc().concat(html) + "</br>");
                }else {
                    //没简介（中文名
                    method.setBrief(html);
                }
            }
        }
    }

    /**
     * 对新方法的所有操作
     * @param name
     * @param isConstruction
     * @param methodList
     * @param startNum
     */
    public void newMethod(String name, Boolean isConstruction, LinkedList<Method> methodList, int startNum){
        method = new Method(control,isConstruction, name, startNum, getEndLine(lineList, startNum, '{', '}'));
        if (isConstruction && "constructionMethod".equals(name)){
            //构造方法
            method.setDesc("构造方法");
            method.setBrief("构造方法");
            method.getAbnormal().add(Method.Abnormal.HASNOCALL.getCode());
            method.getParams().add(new Param("el"));
            method.getParams().add(new Param("options"));
        }else {
            //获取参数
            Matcher paramMatcher = PARAM.matcher(lineList.get(startNum).trim().replaceAll(" ",""));
            while (paramMatcher.find()) {
                method.getParams().add(new Param(paramMatcher.group()));
            }
            if (name.startsWith("$")){
                method.setAccess("protected");
            }
        }
        method.setFileName(control.getFileName()+"-"+name);
        methodList.add(method);
        methodMap.put(method.getFileName(),method);
        control.getMethods().add(method);
    }

    /**
     * -转驼峰
     *
     * @param param 源字符串
     * @return 转换后的字符串
     */
    public String joiner2Camel(String param, char joinner) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == joinner) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 驼峰法转 -
     *
     * @param line 源字符串
     * @return 转换后的字符串
     */
    public String camel2Joiner(String line, char joiner) {
        if (line==null||"".equals(line.trim())){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        sb.append(line.substring(0,1));
        line = line.substring(1,line.length());
        for (int i = 0; i < line.length(); i++) {
            char c=line.charAt(i);
            if (Character.isUpperCase(c)){
                sb.append(joiner);
                sb.append(Character.toLowerCase(c));
            }else{
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 获取当前控件
     * @param controlList 控件表
     * @param lineNum 当前行
     * @return
     */
    public Control getControl(LinkedList<Control> controlList, int lineNum) {
        if (controlList.size() == 0) {
            return null;
        }
        //获取当前控件
        Control control = controlList.getLast();
        if (lineNum > control.getEndLine()) {
            controlList.removeLast();
            getControl(controlList, lineNum);
        }
        return control;
    }

    /**
     * 获取当前方法
     * @param methodList 方法表
     * @param lineNum 当前行
     * @return
     */
    private Method getMethod(LinkedList<Method> methodList, int lineNum) {
        if (methodList.size() == 0) {
            return null;
        }
        //获取当前方法
        Method method = methodList.getLast();
        if (lineNum > method.getEndLine()) {
            methodList.removeLast();
            getMethod(methodList, lineNum);
        }
        return method;
    }

    /**
     * 递归法判断是否冲突
     */
    public String conflict(Control control, Variable variable) {
        Control parent = control.getParentNode();
        if (parent != null) {
            if (parent.getVariables().contains(variable)) {
                System.out.println("变量可能重名：子控件文件路径："+variable.getControl().getPathFrom()+",控件名："+variable.getControl().getName()+",变量名："+variable.getName()+";父控件文件路径:"+parent.getPathFrom()+",控件名："+parent.getName()+"");
                return "可能与"+parent.getPathFrom()+"文件中,"+parent.getName()+"控件下变量重名";
            } else {
                return conflict(parent, variable);
            }
        }
        return "";
    }

    public Boolean startWithIgnoreCase(String src,String obj) {
        return obj.length() <= src.length() && src.substring(0, obj.length()).equalsIgnoreCase(obj);
    }
}
