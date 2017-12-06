package com.ecui;

import com.ecui.domain.Control;
import com.ecui.domain.Method;
import com.ecui.domain.Variable;
import com.ecui.utils.EcuiApiUtils;
import com.ecui.utils.FileUtils;
import com.ruixus.smarty4j.Context;
import com.ruixus.smarty4j.Engine;
import com.ruixus.smarty4j.Template;

import java.io.*;
import java.util.*;

import static com.ecui.utils.FileUtils.fileCopy;
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
            //判断属性是否有简介
            if (control.getBrief()==null|| "".equals(control.getBrief())){
                System.out.println("控件未添加中文名(简介)：文件路径："+control.getPathFrom()+",控件名："+control.getName());
            }
            //判断私有变量是否有冲突
            LinkedHashSet<Variable> variables = control.getVariables();
            variables.forEach(variable -> {
                //判断私有变量是否有冲突
                variable.setRepeat(Variable.conflict(control,variable));
                //判断变量是否有简介
                if (variable.getDesc()==null|| "".equals(variable.getDesc())){
                    System.out.println("变量未添加简介：文件路径："+control.getPathFrom()+
                            ",所属控件："+control.getName()+
                            ",变量名："+variable.getName()
                    );
                }
            });
            //判断方法(事件)是否有简介
            LinkedHashSet<Method> methods = control.getMethods();
            methods.forEach(method -> {
                if (method.getBrief()==null|| "".equals(method.getBrief())){
                    System.out.println("方法(事件)未添加简介：文件路径："+control.getPathFrom()+
                            ",所属控件："+control.getName()+
                            ",方法名："+method.getName()
                    );
                }
            });
            controlMapList.add(control.toMap());
        });
        //方法详情
        List<Map<String,Object>> methodSetList = new ArrayList<>();
        methodSet.forEach(method -> methodSetList.add(method.toMap()));

        //生成文件
        Engine engine = new Engine();
        Context context = new Context();
        context.set("controlTrees",controlList);
        context.set("controls",controlMapList);
        context.set("methods",methodSetList);
        //分别加载模板
        Template controlsTem = engine.getTemplate("controls.tpl");
        Template controlTem = engine.getTemplate("control.tpl");
        Template methodTem = engine.getTemplate("method.tpl");
        //控件树
        String finalDocPath = jarPath+"/doc/";
        String indexPath = new File(finalDocPath).getAbsolutePath()+"\\";
        judeDirExists(indexPath+"0");
        FileOutputStream fileWriter = new FileOutputStream(new File(indexPath+"controlTree.html").getAbsoluteFile());
        controlsTem.merge(context, fileWriter);

        //每个控件
        String controlPath = new File(finalDocPath +"control").getAbsolutePath()+"\\";
        judeDirExists(controlPath+"0");
        controlMap.forEach((s, control) -> {
            try {
                FileOutputStream controlWriter = new FileOutputStream(controlPath+control.getFileName()+".html");
                context.set("control", control.toMap());
                controlTem.merge(context, controlWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //每个方法
        String methodPath = new File(finalDocPath +"method/").getAbsolutePath()+"\\";
        judeDirExists(methodPath+"0");
        methodSet.forEach(method -> {
            try {
                FileOutputStream controlWriter = new FileOutputStream(methodPath+method.getFileName()+".html");
                context.set("method",method.toMap());
                methodTem.merge(context, controlWriter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //复制必要的js，css文件
        fileCopy("common/appliesto2.js",finalDocPath+"common/appliesto2.js");
        fileCopy("common/browdata.js",finalDocPath+"common/browdata.js");
        fileCopy("common/common.js",finalDocPath+"common/common.js");
        fileCopy("common/common.css",finalDocPath+"common/common.css");
        fileCopy("common/prettify.css",finalDocPath+"common/prettify.css");
        fileCopy("common/prettify.js",finalDocPath+"common/prettify.js");
        fileCopy("common/toolbar.js",finalDocPath+"common/toolbar.js");
    }
}