package com.ecui;

import com.ecui.domain.Control;
import com.ecui.domain.Method;
import com.ecui.domain.Variable;
import com.ecui.utils.EcuiApiUtils;
import com.ecui.utils.FileUtils;
import com.ruixus.smarty4j.Context;
import com.ruixus.smarty4j.Engine;
import com.ruixus.smarty4j.Template;

import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.ecui.utils.FileUtils.judeDirExists;

/**
 * @author chentiancheng
 */
public class Start {
    public static void main(String[] args) throws Exception {

        String jarWholePath = Start.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            jarWholePath = java.net.URLDecoder.decode(jarWholePath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String jarPath = new File(jarWholePath).getParentFile().getAbsolutePath();

        EcuiApiUtils ecuiApiUtils = new EcuiApiUtils();
        //获得所有文件集合
        Set<File> fileSet = FileUtils.getFileLists(new File(jarPath+"/src").getAbsolutePath(),".js");
        //获得所有控件节点,和对应关系
        ecuiApiUtils.scanFile(fileSet);
        Map<String,Control> controlMap = ecuiApiUtils.getControlMap();
        Set<Method> methodSet = ecuiApiUtils.getMethodSet();

        //根据获得的节点set,生成树,并获取所有根节点集合
        List<Control> controlList = Control.getTree(controlMap);
        //生成json,控件集合树
        //控件详情
        List<Map<String,Object>> controlMapList = new ArrayList<>();
        controlMap.forEach((s, control) -> {
            //判断私有变量是否有冲突
            LinkedHashSet<Variable> variables = control.getVariables();
            variables.forEach(variable -> variable.setRepeat(Variable.conflict(control,variable)));
            controlMapList.add(control.toMap());
        });
        //变量详情
        List<Map<String,Object>> methodSetList = new ArrayList<>();
        methodSet.forEach(method -> methodSetList.add(method.toMap()));

        //生成文件
        Engine engine = new Engine();
        engine.setTemplatePath(jarWholePath);
        Context context = new Context();
        context.set("controlTrees",controlList);
        context.set("controls",controlMapList);
        context.set("methods",methodSetList);
        //分别加载模板
        Template controlsTem = engine.getTemplate("controls.tpl");
        Template controlTem = engine.getTemplate("control.tpl");
        Template methodTem = engine.getTemplate("method.tpl");
        //控件树
        String finalJarWholePath = jarWholePath;
        String indexPath = new File(finalJarWholePath +"index").getAbsolutePath()+"\\";
        judeDirExists(indexPath+"0");
        FileWriter fileWriter = new FileWriter(new File(indexPath+"controlTree.html").getAbsoluteFile());
        controlsTem.merge(context, fileWriter);

        //每个控件
        String controlPath = new File(finalJarWholePath +"control").getAbsolutePath()+"\\";
        judeDirExists(controlPath+"0");
        controlMap.forEach((s, control) -> {
            try {
                FileWriter controlWriter = new FileWriter(controlPath+control.getFileName()+".html");
                context.set("control", control.toMap());
                controlTem.merge(context, controlWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //每个方法
        String methodPath = new File(finalJarWholePath +"method/").getAbsolutePath()+"\\";
        judeDirExists(methodPath+"0");
        methodSet.forEach(method -> {
            try {
                FileWriter controlWriter = new FileWriter(methodPath+method.getFileName()+".html");
                context.set("method",method.toMap());
                methodTem.merge(context, controlWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}