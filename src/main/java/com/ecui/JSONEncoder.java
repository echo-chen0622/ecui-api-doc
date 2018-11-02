package com.ecui;

import static org.objectweb.asm.Opcodes.*;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.ruixus.smarty4j.MessageFormat;
import com.ruixus.smarty4j.util.DynamicClassLoader;

/**
 * JSON编码器, 允许自定义序列化对象的处理方式, 例如: <code>
 * Map<Class<?>, ISerialize> custom = new HashMap<Class<?>, ISerialize>();
 * custom.put(java.util.Date.class, new ISerialize {
 *   public String serialize(Object o) {
 *     return new SimpleDateFormat((Date) o, "yyyy-MM-dd");
 *   }
 * });
 * JSONEncoder.encoder(o, custom);
 * </code>
 *
 * @version 0.1.5, 2009/06/01
 * @author 欧阳先伟
 * @since Common 0.1
 */
public class JSONEncoder {

    /**
     * 序列化对象接口, 用于自定义特定的序列化对象方式
     */
    public static abstract class Serialize {

        protected void serializeValue(StringBuilder sb, Object o, Collection<Object> used,
                                      Map<Class<?>, Serialize> cached) {
            if (o == null) {
                sb.append("null");
            } else if (o instanceof Boolean || o instanceof Number) {
                sb.append(o.toString());
            } else if (o instanceof String || o instanceof Character) {
                sb.append(o.toString().replace("\\", "\\\\").replace("\"", "\\\""));
            } else {
                serializeObject(sb, o, used, cached);
            }
        }

        protected void serializeObject(StringBuilder sb, Object o, Collection<Object> used,
                                       Map<Class<?>, Serialize> cached) {
            // 防止在属性递归序列化中，一个对象被序列化多次, 避免死循环
            if (used.contains(o)) {
                sb.append("null");
                return;
            }
            used.add(o);
            if (o instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) o;
                sb.append('{');
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    sb.append('"');
                    sb.append(entry.getKey().toString());
                    sb.append('"');
                    sb.append(':');
                    serializeValue(sb, entry.getValue(), used, cached);
                    sb.append(',');
                }
                sb.setCharAt(sb.length() - 1, '}');
            } else if (o instanceof List) {
                sb.append('[');
                for (Object value : (List<?>) o) {
                    serializeValue(sb, value, used, cached);
                    sb.append(',');
                }
                sb.setCharAt(sb.length() - 1, ']');
            } else if (o.getClass().isArray()) {
                sb.append('[');
                int len = Array.getLength(o);
                for (int i = 0; i < len; i++) {
                    serializeValue(sb, Array.get(o, i), used, cached);
                    sb.append(',');
                }
                sb.setCharAt(sb.length() - 1, ']');
            } else {
                cached.get(o.getClass()).serialize(sb, o, used, cached);
            }
            used.remove(o);
        }

        /**
         * 序列化对象方法
         *
         * @param o
         *            需要序列化的对象
         * @return 对象对应的字符串
         */
        public abstract void serialize(StringBuilder sb, Object o, Collection<Object> used,
                                       Map<Class<?>, Serialize> cached);
    }

    private static final Map<Class<?>, Serialize> global = new HashMap<Class<?>, Serialize>();

    /**
     * 将数据对象JSON序列化
     *
     * @param o
     *            需要序列化的JSON对象
     * @return 序列化的JSON字符串
     */
    public static String encode(Object o) {
        return encode(o, global);
    }

    /**
     * 按指定的规则将数据对象JSON序列化
     *
     * @param o
     *            需要序列化的JSON对象
     * @param cached
     *            自定义序列化对象配置信息
     * @return 序列化的JSON字符串
     */
    public static String encode(Object o, Map<Class<?>, Serialize> cached) {
        StringBuilder sb = new StringBuilder(256);
        serialize(sb, o, new ArrayList<Object>(), cached == null ? global : cached);
        return sb.toString();
    }

    private static Serialize buildSerialize(Class<?> clazz, Map<Class<?>, Serialize> cached) {
        boolean first = true;
        String className = clazz.getName().replace('.', '/');
        String interfaceName = Serialize.class.getName().replace('.', '/');

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;
        cw.visit(V1_5, ACC_PUBLIC, "tpl", null, interfaceName, null);

        // 定义类的构造方法
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, interfaceName, "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "serialize",
                "(Ljava/lang/StringBuilder;Ljava/lang/Object;Ljava/util/Collection;Ljava/util/Map;)V", null, null);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitTypeInsn(CHECKCAST, className);
        mv.visitVarInsn(ASTORE, 2);

        mv.visitVarInsn(ALOAD, 1);
        mv.visitLdcInsn("{");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;");

        try {
            // 序列化JavaBean可读属性
            for (PropertyDescriptor prop : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                Method accessor = prop.getReadMethod();
                if (accessor == null) {
                    continue;
                }
                String name = prop.getName();
                // class属性不需要序列化
                if ("class".equals(name)) {
                    continue;
                }

                mv.visitLdcInsn((first ? "" : ",") + "\"" + name + "\":");
                first = false;
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");

                Class<?> type = accessor.getReturnType();
                if (type == int.class) {
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, accessor.getName(), "()I");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                } else if (type == long.class) {
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, accessor.getName(), "()J");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                } else if (type == short.class) {
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, accessor.getName(), "()S");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "toString", "(S)Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                } else if (type == byte.class) {
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, accessor.getName(), "()B");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toString", "(B)Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                } else if (type == double.class) {
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, accessor.getName(), "()D");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                } else if (type == float.class) {
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, accessor.getName(), "()F");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                } else if (type == boolean.class) {
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, accessor.getName(), "()Z");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                } else if (type == char.class) {
                    Label escape = new Label();
                    Label end = new Label();

                    mv.visitLdcInsn("\"");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, accessor.getName(), "()C");
                    mv.visitInsn(DUP);
                    mv.visitLdcInsn('\\');
                    mv.visitJumpInsn(IF_ICMPEQ, escape);

                    mv.visitInsn(DUP);
                    mv.visitLdcInsn('"');
                    mv.visitJumpInsn(IF_ICMPEQ, escape);

                    mv.visitJumpInsn(GOTO, end);

                    mv.visitLabel(escape);
                    mv.visitInsn(SWAP);
                    mv.visitLdcInsn("\\");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitInsn(SWAP);

                    mv.visitLabel(end);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "toString", "(C)Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitLdcInsn("\"");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                } else {
                    String typeName = type.getName().replace('.', '/');

                    Label nonull = new Label();
                    Label end = new Label();

                    mv.visitVarInsn(ALOAD, 2);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, accessor.getName(), "()L" + typeName + ";");
                    mv.visitInsn(DUP);
                    mv.visitJumpInsn(IFNONNULL, nonull);

                    mv.visitInsn(POP);
                    mv.visitLdcInsn("null");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                            "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    mv.visitJumpInsn(GOTO, end);

                    mv.visitLabel(nonull);
                    if (Number.class.isAssignableFrom(type) || type == Boolean.class) {
                        mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "toString", "()Ljava/lang/String;");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    } else if (type == String.class || type == Character.class) {
                        mv.visitInsn(SWAP);
                        mv.visitLdcInsn("\"");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                        mv.visitInsn(SWAP);
                        mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "toString", "()Ljava/lang/String;");
                        mv.visitLdcInsn("\\");
                        mv.visitLdcInsn("\\\\");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace",
                                "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;");
                        mv.visitLdcInsn("\"");
                        mv.visitLdcInsn("\\\"");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace",
                                "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                        mv.visitLdcInsn("\"");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                                "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                    } else {
                        if (!(Map.class.isAssignableFrom(type) || List.class.isAssignableFrom(type)
                                || type.isArray())) {
                            if (!cached.containsKey(type)) {
                                cached.put(type, buildSerialize(type, cached));
                            }
                        }
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitInsn(SWAP);
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitInsn(SWAP);
                        mv.visitVarInsn(ALOAD, 3);
                        mv.visitVarInsn(ALOAD, 4);
                        mv.visitMethodInsn(INVOKEVIRTUAL, interfaceName, "serializeObject",
                                "(Ljava/lang/StringBuilder;Ljava/lang/Object;Ljava/util/Collection;Ljava/util/Map;)V");
                    }

                    mv.visitLabel(end);
                }
            }
        } catch (Exception e) {
        }

        mv.visitLdcInsn('}');
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;");

        mv.visitInsn(POP);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        cw.visitEnd();

        byte[] code = cw.toByteArray();
        try {
            return (Serialize) DynamicClassLoader.defineClass("tpl", code).newInstance();
        } catch (Exception e) {
            // 出现概率极低
            throw new RuntimeException(String.format(MessageFormat.CANNOT_BE_INSTANTIATED, "The parser"));
        }
    }

    /**
     * 序列化对象
     *
     * @param sb
     *            序列化时的字符串缓冲区
     * @param o
     *            需要序列化的对象
     * @param used
     *            正在被递归序列化的对象集合, 防止冲突
     * @param cached
     *            自定义序列化对象配置信息
     */
    private static void serialize(StringBuilder sb, Object o, Collection<Object> used,
                                  Map<Class<?>, Serialize> cached) {
        if (o == null) {
            sb.append("null");
        } else if (o instanceof Boolean || o instanceof Number) {
            sb.append(o.toString());
        } else if (o instanceof String || o instanceof Character) {
            serialize(sb, o.toString());
        } else {
            // 防止在属性递归序列化中，一个对象被序列化多次, 避免死循环
            if (used.contains(o)) {
                sb.append("null");
                return;
            }
            used.add(o);
            if (o instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) o;
                sb.append('{');
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    serialize(sb, entry.getKey().toString(), entry.getValue(), used, cached);
                }
                sb.setCharAt(sb.length() - 1, '}');
            } else if (o instanceof List) {
                sb.append('[');
                for (Object value : (List<?>) o) {
                    serialize(sb, value, used, cached);
                    sb.append(',');
                }
                sb.setCharAt(sb.length() - 1, ']');
            } else if (o.getClass().isArray()) {
                sb.append('[');
                int len = Array.getLength(o);
                for (int i = 0; i < len; i++) {
                    serialize(sb, Array.get(o, i), used, cached);
                    sb.append(',');
                }
                sb.setCharAt(sb.length() - 1, ']');
            } else {
                Class<?> clazz = o.getClass();
                Serialize serial = cached.get(clazz);
                if (serial == null) {
                    cached.put(clazz, null);
                    serial = buildSerialize(clazz, cached);
                    cached.put(clazz, serial);
                }
                serial.serialize(sb, o, used, cached);
            }
            used.remove(o);
        }
    }

    /**
     * 序列化文本
     *
     * @param sb
     *            序列化时的字符串缓冲区
     * @param text
     *            需要序列化的文本
     */
    private static void serialize(StringBuilder sb, String text) {
        sb.append('"');
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                case '\\':
                    sb.append('\\').append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (Character.isISOControl(c)) {
                        sb.append("\\u").append(Integer.toHexString(0x10000 + c), 1, 5);
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    /**
     * 序列化键值对
     *
     * @param sb
     *            序列化时的字符串缓冲区
     * @param name
     *            需要序列化的键名称
     * @param value
     *            需要序列化的值对象
     * @param used
     *            正在被递归序列化的对象集合, 防止冲突
     * @param cached
     *            自定义序列化对象配置信息
     */
    private static void serialize(StringBuilder sb, String name, Object value, Collection<Object> used,
                                  Map<Class<?>, Serialize> cached) {
        serialize(sb, name);
        sb.append(':');
        serialize(sb, value, used, cached);
        sb.append(',');
    }

    public static class Test1 {
        public int getInt() {
            return 15;
        }
    }

    public static class Test {
        public int getInt() {
            return 10;
        }

        public short getShort() {
            return 3;
        }

        public byte getByte() {
            return 1;
        }

        public long getLong() {
            return 10000;
        }

        public double getDouble() {
            return 100.17;
        }

        public float getFloat() {
            return 102;
        }

        public boolean getBoolean() {
            return true;
        }

        public Integer getIObject() {
            return 17;
        }

        public Boolean getBObject() {
            return false;
        }

        public String getNull() {
            return null;
        }

        public String getString() {
            return "\\\"";
        }

        private static final Test1 test1 = new Test1();

        public Test1 getTest1() {
            return test1;
        }

        public Test getTest() {
            return this;
        }

        public char getChar() {
            return '\\';
        }
        public char getChar2() {
            return '"';
        }
    }

    public static void main(String[] args) {
        System.out.println(new Integer(3).toString());
        System.out.println(encode(new Test()));
    }
}
