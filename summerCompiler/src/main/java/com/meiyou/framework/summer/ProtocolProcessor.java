package com.meiyou.framework.summer;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

/**
 * main processor for annotation
 * Created by hxd on 16/3/28.
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ProtocolProcessor extends AbstractProcessor {
    Elements elementUtils;
    Types typeUtils;
    Filer filer;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        //processFunction(roundEnv);

        processProtocol(roundEnv);

        return true;
    }

    private static class ElementHolder {
        TypeElement typeElement;
        String valueName;
        String clazzName;
        String simpleName;

        public ElementHolder(TypeElement typeElement, String valueName, String clazzName, String simpleName) {
            this.typeElement = typeElement;
            this.valueName = valueName;
            this.clazzName = clazzName;
            this.simpleName = simpleName;
        }
    }

    /**
     * 编译期不做强制校验,这样运行 独立模块编译通过
     * 但是一旦使用@ProtocolInterpreter 将触发强制校验
     * 可能引发异常
     *
     * @param roundEnv env
     */
    private void processProtocol(RoundEnvironment roundEnv) {
        Map<String, ElementHolder> shadowMap = collectClassInfo(roundEnv, ProtocolShadow.class, ElementKind.INTERFACE);
        if (shadowMap.keySet().size() == 0) {
            System.out.println("find ProtocolShadow size 0");
            //return;
        }

        for (String value : shadowMap.keySet()) {
            System.out.println("create file for protocol shadow  " + value);
            ProtocolDataClass protocolDataClass = new ProtocolDataClass();
            try {
                String simpleName = shadowMap.get(value).simpleName;
                JavaFileObject fileObject = filer.createSourceFile(ProtocolDataClass.getClassNameForPackage(simpleName), (Element[]) null);
                Writer writer = fileObject.openWriter();
                writer.write(protocolDataClass.generateMiddleClass(simpleName, value));
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Map<String, ElementHolder> protocolMap = collectClassInfo(roundEnv, Protocol.class, ElementKind.CLASS);
        if (protocolMap.keySet().size() == 0) {
            System.out.println("find Protocol size 0");
            //return;
        }

        for (String value : protocolMap.keySet()) {
            System.out.println("create file for protocol  " + value);
            ProtocolDataClass protocolDataClass = new ProtocolDataClass();
            try {
                JavaFileObject fileObject = filer.createSourceFile(value, (Element[]) null);
                Writer writer = fileObject.openWriter();
                //注意这里跟 shadow 传递参数有点不一样
                writer.write(protocolDataClass.generateMiddleClass(value, protocolMap.get(value).clazzName));
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private Map<String, ElementHolder> collectClassInfo(RoundEnvironment roundEnv,
                                                        Class<? extends Annotation> clazz, ElementKind kind) {
        System.out.println("collectClassInfo for" + clazz.getSimpleName());
        Map<String, ElementHolder> map = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(clazz)) {
            if (element.getKind() != kind) {
                throw new IllegalStateException(
                        String.format("@%s annotation must be on a  %s.", element.getSimpleName(), kind.name()));
            }
            try {

                TypeElement typeElement = (TypeElement) element;
                Annotation annotation = element.getAnnotation(clazz);
                Method annotationMethod = clazz.getDeclaredMethod("value");
                String name = (String) annotationMethod.invoke(annotation);
                String clazzName = typeElement.getQualifiedName().toString();
                String simpleName = typeElement.getSimpleName().toString();
                map.put(name, new ElementHolder(typeElement, name, clazzName, simpleName));
                System.out.println("get Annotation from Class :" + simpleName);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        return map;
    }

    private void processEvent(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Event.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                throw new IllegalStateException(
                        String.format("@%s annotation must be on a method.", element.getSimpleName()));
            }
            try {
                TypeElement typeElement = (TypeElement) element;
                Annotation annotation = element.getAnnotation(Event.class);
                Method annotationMethod = Event.class.getDeclaredMethod("value");
                String value = (String) annotationMethod.invoke(annotation);
                String simpleName = typeElement.getSimpleName().toString();
                ProtocolDataClass protocolDataClass = new ProtocolDataClass();
                protocolDataClass.generateMiddleClass(simpleName, value);
                //TODO  生成class
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }

    private void processEventRemote(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(EventRemote.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                throw new IllegalStateException(
                        String.format("@%s annotation must be on a method.", element.getSimpleName()));
            }
            try {
                TypeElement typeElement = (TypeElement) element;
                Annotation annotation = element.getAnnotation(EventRemote.class);
                Method annotationMethod = EventRemote.class.getDeclaredMethod("value");
                String value = (String) annotationMethod.invoke(annotation);
                String simpleName = typeElement.getSimpleName().toString();
                ProtocolDataClass protocolDataClass = new ProtocolDataClass();
                protocolDataClass.generateMiddleClass(value,simpleName);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }


    private void processOnEvent(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(OnEvent.class)) {
            if (element.getKind() != ElementKind.METHOD) {
                throw new IllegalStateException(
                        String.format("@%s annotation must be on a method.", element.getSimpleName()));
            }
            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            String clazzName = enclosingElement.getQualifiedName().toString();
            String simpleName = enclosingElement.getSimpleName().toString();
            String methodName = executableElement.getSimpleName().toString();

        }

    }

    /**
     * private void processFunction(RoundEnvironment roundEnv) {
     * for (Element element : roundEnv.getElementsAnnotatedWith(Function.class)) {
     * <p>
     * if (!(element instanceof ExecutableElement) || element.getKind() !=
     * javax.lang.model.element.ElementKind.METHOD) {
     * throw new IllegalStateException(
     * String.format("@%s annotation must be on a method.", element.getSimpleName()));
     * }
     * <p>
     * ExecutableElement executableElement = (ExecutableElement) element;
     * TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
     * try {
     * Annotation annotation = element.getAnnotation(Function.class);
     * Method annotationMethod = Function.class.getDeclaredMethod("value");
     * String name = (String) annotationMethod.invoke(annotation);
     * <p>
     * List<? extends VariableElement> params = executableElement.getParameters();
     * TypeMirror returnType = executableElement.getReturnType();
     * <p>
     * String targetType = enclosingElement.getQualifiedName().toString();
     * //String classPackage = getPackageName(enclosingElement);
     * <p>
     * String realName = convert2ClassName(name);
     * MetaClass metaClass = MetaClass.newBuilder().
     * className(realName)
     * .classPackage(getPackageName(enclosingElement))
     * .actionName(executableElement.getSimpleName().toString())
     * .params(params)
     * .targetName(targetType).build();
     * JavaFileObject fileObject = filer.createSourceFile(realName, element);
     * Writer writer = fileObject.openWriter();
     * writer.write(metaClass.brewJava());
     * writer.flush();
     * writer.close();
     * } catch (Exception e) {
     * e.printStackTrace();
     * throw new RuntimeException(e);
     * }
     * <p>
     * }
     * }
     **/

    private String convert2ClassName(String name) {
        String tmp = name.replace("-", "").replace(".", "").replace("_", "");
        return tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
    }


    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<String>();
        types.add(Function.class.getCanonicalName());
        types.add(FunctionShadow.class.getCanonicalName());
        types.add(Protocol.class.getCanonicalName());
        types.add(ProtocolShadow.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }


}
