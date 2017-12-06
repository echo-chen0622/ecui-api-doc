package com.ecui.utils;

/**
 * Created with IntelliJ IDEA.
 *
 * @author chentiancheng
 * @date 2017/12/5
 * @time 19:33
 * @describe
 */
public class StringUtils {
    /**
     * -转驼峰
     *
     * @param param 源字符串
     * @return 转换后的字符串
     */
    public static String joiner2Camel(String param, char joinner) {
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
    public static String camel2Joiner(String line, char joiner) {
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
     * 转义
     * @param html
     * @return
     */
    public static String escape(String html) {
        return  html.replaceAll("\"","&quot;")
                .replaceAll("<","&lt;")
                .replaceAll(">","&gt;")
                .replaceAll(" ","&nbsp;");
    }

    /**
     * 是否以期望的obj开头，不区分大小写
     * @param src 目标
     * @param obj 期望
     * @return
     */
    public static Boolean startWithIgnoreCase(String src,String obj) {
        return obj.length() <= src.length() && src.substring(0, obj.length()).equalsIgnoreCase(obj);
    }
}
