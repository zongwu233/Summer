package com.meiyou.framework.summer;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by hxd on 16/6/13.
 */
public class ProtocolDataClass {
    public static String packageName = "com.meiyou.framework.summer.data";
    public static String className = "ProtocolData";
    public static String fullName = packageName + "." + className;
    public static String methodName = "prepareData";

    public ProtocolDataClass() {

    }


    public static String getClassNameForPackage(String simpleName) {
        return packageName + "." + simpleName;
    }


    public static String getValueFromClass(Class middleClass) {
        try {
            Field field = middleClass.getDeclaredField("value");
            return (String) field.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Deprecated
    public String generateDataMap(List<ClazzInfo> clazzInfoList) {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code from Summer. Do not modify!\n");
        builder.append("package ").append(packageName).append(";\n\n");

        builder.append("import java.lang.String;\n");
        builder.append("import java.util.Map;\n");
        builder.append("import java.util.HashMap;\n");
        builder.append("public class ").append(className).append("{\n");
        builder.append("static public Map<String, String> prepareData(){\n");
        builder.append(" Map<String, String> map=new HashMap<>();\n");

        for (ClazzInfo info : clazzInfoList) {
            builder.append("map.put(\"").append(info.clazzName).append("\"").
                    append(",\"").append(info.targetClazzName).append("\"")
                    .append(");\n");
        }
        builder.append("return map;\n");
        builder.append("}\n");

        builder.append("}\n");
        return builder.toString();
    }

    public String generateMiddleClass(String middleClassName, String value) {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code from Summer. Do not modify!\n");
        builder.append("package ").append(packageName).append(";\n\n");

        builder.append("import java.lang.String;\n");
        builder.append("public class ").append(middleClassName).append("{\n");

        builder.append("  public static String value=\"").append(value).append("\";\n");

        builder.append("}\n");
        return builder.toString();

    }

//    public Map<String, ClazzInfo> prepareData() {
//        Map<String, ClazzInfo> map = new HashMap<>();
//        for (ClazzInfo info : clazzInfoList) {
//            map.put(info.clazzName, info);
//        }
//        return map;
//    }

}
