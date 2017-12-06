package com.ecui.utils;

import com.ecui.domain.Control;
import com.ecui.domain.Method;
import com.ecui.domain.Param;
import com.ecui.domain.Variable;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ecui.utils.FileUtils.getEndLine;
import static com.ecui.utils.FileUtils.getStartNoteLine;
import static com.ecui.utils.StringUtils.*;


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
    private LinkedList<Control> currentControlList;
    /**
     * 当前文件方法集合
     */
    private LinkedList<Method> currentMethodList;
    /**
     * 控件集合
     */
    private Map<String, Control> controlMap = new HashMap<String, Control>();
    /**
     * 方法集合
     */
    private Set<Method> methodSet = new HashSet<Method>();
    /**
     * 文件的行列表
     */
    private List<String> lineList;
    /**
     * 当前方法
     */
    private Method currentMethod;
    /**
     * 当前控件
     */
    private Control currentControl;

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

    //getter and setter start

    public Map<String, Control> getControlMap() {
        return controlMap;
    }

    public Set<Method> getMethodSet() {
        return methodSet;
    }

    //getter and setter end

    /**
     * 获取树的各节点
     * @param fileSet
     * @return
     */
    public void scanFile(Set<File> fileSet) {

        //循环遍历所有文件
        for (File file : fileSet){
            //文件地址
            String path = file.getPath();
            Matcher pathMatcher = PATH.matcher(path);
            if (pathMatcher.find()){
                path = pathMatcher.group();
            }
            path = "..."+path;
            //将文件转换成行列表
            lineList = FileUtils.fileToLineList(file);
            //控件及其子控件集合
            currentControlList = new LinkedList<Control>();
            //方法集合
            currentMethodList = new LinkedList<Method>();

            for (int i = 0; i < lineList.size(); i++) {
                //当前所属控件（可能为空，意味着不在任何控件内
                currentControl = getCurrentControl(i);
                //当前所属方法(可能为空
                currentMethod = getCurrentMethod(i);
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
                        if (currentControl != null) {
                            node = currentControl.getName() + "." + node;
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
                }else if(currentControl != null) {
                    //控件内代码
                    //正则匹配
                    Matcher variableMatcher = VARIABLE.matcher(line);
                    Matcher methodMatcher = METHOD.matcher(line);
                    Matcher constructionMethodMatcher = CONSTRUCTIONMETHOD.matcher(line);
                    //找'_'开头的变量
                    while (variableMatcher.find()) {
                        Variable variable = new Variable(variableMatcher.group(), currentControl);
                        if (!currentControl.getVariables().contains(variable)){
                            //没有找到同名变量则新增
                            currentControl.getVariables().add(variable);
                        }
                    }
                    //匹配是否为方法
                    if (constructionMethodMatcher.find()) {
                        //构造方法
                        getNewMethod("constructionMethod",true,i);
                    }else if (methodMatcher.find()){
                        //普通方法
                        if (currentMethod !=null&& currentMethod.getConstruction()){
                            //在构造方法内
                            getNewMethod(methodMatcher.group(),true,i);
                        }else {
                            //不在构造方法内
                            if (currentMethod ==null) {
                                getNewMethod(methodMatcher.group(), false, i);
                                //分析注释
                                anlyzeNotesForMethod();
                            }
                        }
                    }else if (currentMethod !=null){
                        //方法内
                        if (currentMethod.getConstruction()&& "constructionMethod".equals(currentMethod.getName())) {
                            //构造方法内
                            if (line.replaceAll(" ","").contains(".call(this,el,options)")){
                                //包含.call()
                                currentControl.getAbnormal().remove(Control.Abnormal.HASNOCALL.getCode());
                            }
                        }
                    }
                }
            }
        }
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
                System.out.println("注释中找到未知语法;文件:" + currentControl.getPathFrom() + ",行数:" + i + ",语法:" + line);
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
                    currentControl.getVariables().add(new Variable(variableName, currentControl,variableDesc));
                }else {
                    //存入控件的详细描述
                    String html = escape(line);
                    example = example.concat(html+"</br>");
                }
            }
        }
        currentControl.setExample(example);
    }

    /**
     * 解析控件的注释
     */
    private void anlyzeNotesForControl() {
        int notesStartLine = getStartNoteLine(lineList, currentControl.getStartLine()-1,"*/","/**");
        if (notesStartLine == -1){
            return;
        }
        //解析注释
        for (int i = notesStartLine+1; i < currentControl.getStartLine()-1; i++) {
            String line = lineList.get(i).trim();
            currentControl.getNotes().add(line);
            //去掉开头的'*'
            line = line.substring(1).trim();

            if (line.startsWith("@")){
                if (startWithIgnoreCase(line,"@public")){
                    currentControl.setAccess("public");
                }else if (startWithIgnoreCase(line,"@protected")){
                    currentControl.setAccess("protected");
                }else if (startWithIgnoreCase(line,"@private")){
                    currentControl.setAccess("private");
                }else if (startWithIgnoreCase(line,"@currentControl")){
                    currentControl.setType("currentControl");
                    currentControl.setStyle(lineList.get(currentControl.getStartLine()+2).trim().substring(1,lineList.get(currentControl.getStartLine()+2).trim().length()-2));
                }else if (startWithIgnoreCase(line,"@control")){
                    currentControl.setType("control");
                }else if (startWithIgnoreCase(line,"@unit")){
                    currentControl.setType("unit");
                }else {
                    System.out.println("注释中找到未知语法;文件:"+ currentControl.getPathFrom()+",行数:"+i+",语法:"+line);
                }
            }else if (!"".equals(line)&&!"options 属性：".equals(line)&&!"options 对象支持的属性如下：".equals(line)){
                Matcher optionsMather = OPTIONS.matcher(line);
                if (optionsMather.find()){
                    String paramName = optionsMather.group();
                    String paramDesc = line.replace(paramName,"").trim();
                    currentControl.getOptionParams().add(new Param(paramName,paramDesc));
                }else {
                    String html = escape(line);
                    if (!"".equals(currentControl.getBrief())) {
                        //有简介
                        currentControl.setDesc(currentControl.getDesc().concat(html) + "</br>");
                    } else {
                        //没简介（中文名
                        currentControl.setBrief(html);
                    }
                }
            }
        }
    }

    /**
     * 解析方法的注释
     */
    private void anlyzeNotesForMethod() {
        int notesStartLine = getStartNoteLine(lineList, currentMethod.getStartLine()-1,"*/","/**");
        if (notesStartLine == -1){
            return;
        }
        //解析注释
        for (int i = notesStartLine+1; i < currentMethod.getStartLine()-1; i++) {
            String line = lineList.get(i).trim();
            currentMethod.getNotes().add(line);
            line = line.substring(1).trim();
            if (line.startsWith("@")){
                if (startWithIgnoreCase(line,"@param")){
                    String lineForParam = line.substring(6).trim();
                    for (Param param : currentMethod.getParams()){
                        if (lineForParam.contains(param.getName())){
                            param.setDesc(lineForParam);
                        }
                    }
                }else if (startWithIgnoreCase(line,"@override")){
                    currentMethod.setOverride(true);
                }else if (startWithIgnoreCase(line,"@return")){
                    currentMethod.setResult(line.substring(7).trim());
                }else if (startWithIgnoreCase(line,"@public")){
                    currentMethod.setAccess("public");
                }else if (startWithIgnoreCase(line,"@protected")){
                    currentMethod.setAccess("protected");
                }else if (startWithIgnoreCase(line,"@private")){
                    currentMethod.setAccess("private");
                }else if (startWithIgnoreCase(line,"@event")) {
                    currentMethod.setEvent(true);
                }else {
                    System.out.println("注释中找到未知语法;文件:"+ currentMethod.getControl().getPathFrom()+",行数:"+i+",语法:"+line+"");
                }
            }else if (!"".equals(line)){
                String html = escape(line);
                if (!"".equals(currentMethod.getBrief())){
                    //有简介
                    currentMethod.setDesc(currentMethod.getDesc().concat(html) + "</br>");
                }else {
                    //没简介（中文名
                    currentMethod.setBrief(html);
                }
            }
        }
    }

    /**
     * 获得一个新控件
     * @param node
     * @param parentNode
     * @param path
     * @param i
     */
    public void getNewControl(String node,String parentNode,String path,int i){
        //设置根节点的父节点为空
        if ("null".equals(parentNode)) {
            parentNode = null;
        } else {
            //去掉开头的"ui."
            parentNode = parentNode.substring(3);
        }
        //集中处理控件节点
        currentControl = new Control();
        currentControl.setName(node);
        currentControl.setParentName(parentNode);
        currentControl.setStartLine(i);
        if (parentNode!=null) {
            currentControl.getAbnormal().add(Control.Abnormal.HASNOCALL.getCode());
        }
        currentControl.setEndLine(getEndLine(lineList, i, '(', ')'));
        currentControl.setFileName(node.replace('.','-'));
        currentControl.setPathFrom(path);
        //处理紧挨控件上面的注释
        anlyzeNotesForControl();
        currentControlList.add(currentControl);
        controlMap.put(node,currentControl);
    }

    /**
     * 对新方法的所有操作
     * @param name
     * @param isConstruction
     * @param startNum
     */
    public void getNewMethod(String name, Boolean isConstruction, int startNum){
        currentMethod = new Method(currentControl,isConstruction, name, startNum, getEndLine(lineList, startNum, '{', '}'));
        if (isConstruction && "constructionMethod".equals(name)){
            //构造方法
            currentMethod.setDesc("构造方法");
            currentMethod.setBrief("构造方法");
            currentMethod.getParams().add(new Param("el"));
            currentMethod.getParams().add(new Param("options"));
        }else {
            //获取参数
            Matcher paramMatcher = PARAM.matcher(lineList.get(startNum).trim().replaceAll(" ",""));
            while (paramMatcher.find()) {
                currentMethod.getParams().add(new Param(paramMatcher.group()));
            }
            if (name.startsWith("$")){
                currentMethod.setAccess("protected");
            }
        }
        currentMethod.setFileName(currentControl.getFileName()+"-"+name);
        currentMethodList.add(currentMethod);
        methodSet.add(currentMethod);
        currentControl.getMethods().add(currentMethod);
    }

    /**
     * 获取当前控件
     * @param lineNum 当前行
     * @return
     */
    public Control getCurrentControl(int lineNum) {
        if (currentControlList.size() == 0) {
            return null;
        }
        //获取当前控件
        Control control = currentControlList.getLast();
        if (lineNum > control.getEndLine()) {
            currentControlList.removeLast();
            getCurrentControl(lineNum);
        }
        return control;
    }

    /**
     * 获取当前方法
     * @param lineNum 当前行
     * @return
     */
    private Method getCurrentMethod(int lineNum) {
        if (currentMethodList.size() == 0) {
            return null;
        }
        //获取当前方法
        Method method = currentMethodList.getLast();
        if (lineNum > method.getEndLine()) {
            currentMethodList.removeLast();
            getCurrentMethod(lineNum);
        }
        return method;
    }

}
