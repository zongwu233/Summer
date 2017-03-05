package com.meiyou.framework.summer;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.VariableElement;

/**
 * Created by hxd on 16/3/28.
 */
@Deprecated
public class MetaClass {
    private String classPackage;
    private String className;
    private String targetName;
    private String actionName;
    private List<? extends VariableElement> params;

    private MetaClass(Builder builder) {
        classPackage = builder.classPackage;
        className = builder.className;
        targetName = builder.targetName;
        actionName = builder.actionName;
        params = builder.params;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public String brewJava() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code from Summer. Do not modify!\n");
        builder.append("package ").append(classPackage).append(";\n\n");

        builder.append("import ").append(FunctionMeta.class.getName()).append(";\n");
        builder.append("import java.util.LinkedHashMap;\n");
        builder.append("public class ").append(className).append(" implements ").
                append(FunctionMeta.class.getSimpleName()).append("{\n");

        builder.append("@Override\n")
                .append("public String targetName() {\n")
                .append("   return \"").append(targetName).append("\";\n")
                .append("}\n");

        builder.append("@Override\n")
                .append("public String actionName() {\n")
                .append("   return \"").append(actionName).append("\";\n")
                .append("}\n");

        /**
         *   return new Class<?>[]{String.class, Context.class};
         }
         */
        if (params != null && !params.isEmpty()) {
            builder.append("@Override\n")
                    .append(" public Class<?>[] paramKeyTypes() {\n")
                    .append("  return new Class<?>[]{ ");
            List<String> names = new ArrayList<String>();
            for (VariableElement element : params) {
                String clazzName = element.asType().toString();
                names.add(clazzName + ".class");
            }
            builder.append(join(names, ","));
            builder.append("};\n");
        }
        builder.append("}\n");
        builder.append("}\n");
        return builder.toString();
    }



    public static final class Builder {
        private String classPackage;
        private String className;
        private String targetName;
        private String actionName;
        private List<? extends VariableElement> params;

        private Builder() {
        }

        public Builder classPackage(String val) {
            classPackage = val;
            return this;
        }

        public Builder className(String val) {
            className = val;
            return this;
        }

        public Builder targetName(String val) {
            targetName = val;
            return this;
        }

        public Builder actionName(String val) {
            actionName = val;
            return this;
        }

        public Builder params(List<? extends VariableElement> val) {
            params = val;
            return this;
        }

        public MetaClass build() {
            return new MetaClass(this);
        }
    }
    private static String join(Collection var0, String var1) {
        StringBuffer var2 = new StringBuffer();
        for (Iterator var3 = var0.iterator(); var3.hasNext(); var2.append((String) var3.next())) {
            if (var2.length() != 0) {
                var2.append(var1);
            }
        }
        return var2.toString();
    }
}
