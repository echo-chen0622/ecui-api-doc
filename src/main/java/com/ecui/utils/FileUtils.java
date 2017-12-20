package com.ecui.utils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 *
 * @author chentiancheng
 * @date 2017/11/21
 * @time 10:42
 * @describe
 */
public class FileUtils {

    /**
     * 获取所有指定目录下所有指定后缀文件,可以传多个
     * @param path 目录
     * @param types 文件后缀，如 .js,可传多个; 不传或为空字符串时默认读取所有文件
     * @return 符合条件的文件集合
     */
    public static Set<File> getFileLists(String path, String... types){
        File file=new File(path);
        Set<File> fileSet = new HashSet<File>();
        return getFile(file,fileSet,types);
    }

    /**
     * 添加文件及其所有子目录下的指定后缀文件
     * @param file 文件
     * @param fileSet 文件集合
     * @param types 文件后缀，如 .js,可传多个; 不传或为空字符串时默认读取所有文件
     * @return 符合条件的文件集合
     */
    public static Set<File> getFile(File file, Set<File> fileSet, String... types) {
        try {
            if(file.isFile()) {
                if (types == null||types.length==0){
                    types = new String[]{""};
                }
                for (String type : types) {
                    if (file.getName().endsWith(type)) {
                        fileSet.add(file);
                    }
                }
            }
            else {
                //如果是文件夹，声明一个数组放文件夹和他的子文件
                File[] f=file.listFiles();
                //遍历文件件下的文件，并获取路径
                for (File file2 : f != null ? f : new File[0]) {
                    getFile(file2, fileSet, types);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return fileSet;
    }

    /**
     * 将指定文件转换成StringList
     * @param file
     * @return String
     */
    public static List<String> fileToLineList(File file){
        BufferedReader reader;
        List<String> lineList = new ArrayList<String>();
        try {
            String line;
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            reader = new BufferedReader(isr);
            while ((line = reader.readLine())!=null){
                lineList.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lineList;
    }

    /**
     * 创建文件
     * @param path
     * @param fileName
     * @param fileContent
     */
    public static void createFile(String path,String fileName, String fileContent){
        try {
            //判断文件夹是否存在
            judeDirExists(path+fileName);
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(new FileOutputStream(path+fileName), "utf-8");
            oStreamWriter.append(fileContent);
            oStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断文件夹是否存在,若不存在，新增一个
     */
    public static void judeDirExists(String path) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    /**
     * 找到注释开始行
     * @return
     */
    public static int getStartNoteLine(List<String> lineList,int startLineNum,String endLine,String startLine){
        if (!lineList.get(startLineNum).trim().equals(endLine)){
            return -1;
        }
        for (int i = startLineNum-1; i >= 0; i--) {
            String line = lineList.get(i).trim();
            if (startLine.equals(line)){
                return i;
            }
        }
        return -1;
    }

    /**
     * 找到注释最后行
     * @param lineList
     * @param startLine
     * @param startRule
     * @param endRule
     * @return
     */
    public static int getEndLine(List<String> lineList,int startLine,String startRule,String endRule){
        int endLine = -1;
        if (lineList.get(startLine).trim().equals(startRule)){
            for (int scanLine = startLine; scanLine < lineList.size(); scanLine++) {
                String line = lineList.get(scanLine).trim();
                if (line.equals(endRule)) {
                    endLine = scanLine;
                    break;
                }
            }
        }
        return endLine;
    }

    public static int getEndLine(List<String> lineList,int startLine,char startRule,char endRule){
        //第一行没有开始字符
        if (!lineList.get(startLine).contains(String.valueOf(startRule))){
            return startLine;
        }
        int endLine = startLine;
        int num = 0;
        A : for (int scanLine = startLine;scanLine<lineList.size();scanLine++){
            String line = lineList.get(scanLine);
            for (int i=0;i<line.length();i++){
                char c = line.charAt(i);
                if (c == startRule){
                    num++;
                }else if (c == endRule){
                    num--;
                    if (num == 0&&scanLine>startLine){
                        endLine = scanLine;
                        break A;
                    }
                }
            }
        }
        return endLine;
    }


    /**
     * 从JAR中复制文件到磁盘
     * @param srcFilePath：源路径，既JAR包中的资源文件，路径相对于CLASSPATH
     * @param destFilePath：目标路径，磁盘上的任意路径，绝对路径（一般为用户选择的文件夹路径）
     * @return int：返回执行后的状态；0：失败；1：成功；（可以扩充其它状态）
     */
    public static int fileCopy(String srcFilePath, String destFilePath){
        int flag = 0;
        File destFile = new File(destFilePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        try {
            BufferedInputStream fis = new BufferedInputStream(ClassLoader.getSystemResourceAsStream(srcFilePath));
            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int c;
            while ((c = fis.read(buf)) != -1) {
                fos.write(buf, 0, c);
            }
            fis.close();
            fos.close();
            flag = 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }
}
