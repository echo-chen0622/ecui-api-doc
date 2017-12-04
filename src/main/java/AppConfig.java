import com.ecui.utils.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.ecui.utils.FileUtils.makeFile;

/**
 * @author chentiancheng
 */
public class AppConfig {
    public static void main(String[] args) {

        String jarWholePath = AppConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            jarWholePath = java.net.URLDecoder.decode(jarWholePath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String jarPath = new File(jarWholePath).getParentFile().getAbsolutePath();

        EcuiApiUtils ecuiApiUtils = new EcuiApiUtils();
        //获得所有文件集合
        Set<File> fileSet = FileUtils.getFileLists(jarPath+"\\src",".js");
        //获得所有控件节点,和对应关系
        ecuiApiUtils.scanFile(fileSet);
        Map<String,Control> controlMap = ecuiApiUtils.getControlMap();
        Map<String,Method> methodMap = ecuiApiUtils.getMethodMap();

        //根据获得的节点set,生成树,并获取所有根节点集合
        List<Control> controlList = Control.getTree(controlMap);
        //生成json,控件集合树
        String path = jarPath+"\\doc\\WEB-INF\\";

        //控件详情
        List<String> controlJson = new ArrayList<>();
        controlMap.forEach((s, control) -> {
            //判断私有变量是否有冲突
            LinkedHashSet<Variable> variables = control.getVariables();
            variables.forEach(variable -> variable.setRepeat(ecuiApiUtils.conflict(control,variable)));
            controlJson.add(control.toJson());
        });

        //方法详情
        List<String> methodJson = new ArrayList<>();
        methodMap.forEach((s, method) ->{
            if (!method.getConstruction()) {
                methodJson.add(method.toJson());
            }
        });
        makeFile(path,"controls.json", controlList.toString());
        makeFile(path,"control.json",controlJson.toString());
        makeFile(path,"method.json",methodJson.toString());
    }
}