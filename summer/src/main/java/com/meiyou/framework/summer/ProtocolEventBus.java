package com.meiyou.framework.summer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * for event bus 事件总线
 * Created by hxd on 16/6/20.
 */

class ProtocolEventBus {
    private static ProtocolEventBus ourInstance = new ProtocolEventBus();

    public static ProtocolEventBus getInstance() {
        return ourInstance;
    }

    private ConcurrentHashMap<String, List<OnEventClassInfo>> eventRemoteMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class, List<Method>> cacheMethodMap = new ConcurrentHashMap<>();
    private Handler mainHandler;
    private Handler threadHandler;

    private ProtocolEventBus() {
        mainHandler = new Handler(Looper.getMainLooper());
        BackgroundHandler backgroundHandler = new BackgroundHandler("bg-handler-thread",
                Process.THREAD_PRIORITY_BACKGROUND);
        backgroundHandler.start();
        threadHandler = new Handler(backgroundHandler.getLooper());
    }

    public synchronized void register(Object object) {
        if (object == null) {
            throw new RuntimeException("register null!");
        }
        if (isRegistered(object)) {
            return;
        }
        List<Method> methodList = getOnEventMethod(object);
        if (methodList == null || methodList.isEmpty()) {
            throw new RuntimeException("no OnEvent Annotation method!!");
        }
        for (Method method : methodList) {
            Class<?>[] paramsClazz = method.getParameterTypes();
            if (paramsClazz == null || paramsClazz.length > 1) {
                throw new RuntimeException("onEvent method params invalid!!");
            }
            Class paramClazz = paramsClazz[0];
            OnEvent onEvent = method.getAnnotation(OnEvent.class);
            String value = onEvent.value();
            int thread = onEvent.exec();
            List<OnEventClassInfo> onEventClassInfoList = eventRemoteMap.get(value);
            if (onEventClassInfoList == null) {
                onEventClassInfoList = new CopyOnWriteArrayList<>();
                List<OnEventClassInfo> tmp = eventRemoteMap.putIfAbsent(value, onEventClassInfoList);
                if (tmp != null) {
                    onEventClassInfoList = tmp;
                }
            }
            onEventClassInfoList.add(new OnEventClassInfo(object.getClass(), object, method, paramClazz, thread));
        }
    }

    private List<Method> getOnEventMethod(Object object) {
        Class clazz = object.getClass();
        if (cacheMethodMap.containsKey(clazz)) {
            return cacheMethodMap.get(clazz);
        }
        Method[] declaredMethods = clazz.getDeclaredMethods();
        if (declaredMethods != null) {
            for (Method method : declaredMethods) {
                Annotation[] annotations = method.getDeclaredAnnotations();
                if (annotations != null) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof OnEvent) {
                            //cacheMethodMap.put(clazz, method);
                            List<Method> methodList = cacheMethodMap.get(clazz);
                            if (methodList == null) {
                                methodList = new ArrayList<>();
                                List<Method> tmp = cacheMethodMap.putIfAbsent(clazz, methodList);
                                if (tmp != null) {
                                    methodList = tmp;
                                }
                            }
                            methodList.add(method);
                        }
                    }
                }
            }
        }
        return cacheMethodMap.get(clazz);
    }

    public void unRegister(Object object) {
        for (List<OnEventClassInfo> onEventClassInfoList : eventRemoteMap.values()) {
            for (OnEventClassInfo onEventClassInfo : onEventClassInfoList) {
                if (onEventClassInfo != null && onEventClassInfo.obj == object) {
                    onEventClassInfoList.remove(onEventClassInfo);
                    break;
                }
            }
        }
    }

    public boolean isRegistered(Object object) {
        for (List<OnEventClassInfo> onEventClassInfoList : eventRemoteMap.values()) {
            for (OnEventClassInfo onEventClassInfo : onEventClassInfoList) {
                if (onEventClassInfo != null && onEventClassInfo.obj == object) {
                    return true;
                }
            }
        }
        return false;
    }


    public void post(Object object) {
        try {
            Event event = object.getClass().getAnnotation(Event.class);
            if (event == null) {
                throw new RuntimeException(object.getClass() + "has no Event Annotation!");
            }
            String value = event.value();

            List<OnEventClassInfo> onEventClassInfoList = eventRemoteMap.get(value);

            if (onEventClassInfoList == null || onEventClassInfoList.isEmpty()) {
                System.out.print("OnEventClassInfo is null!!");
                return;
            }

            for (final OnEventClassInfo info : onEventClassInfoList) {
                final Object remoteObj = copyToTarget(object, object.getClass(), info.paramClazz);
                if (remoteObj == null) {
                    throw new RuntimeException(object.getClass() + " copy To target failed!!");
                }
                boolean isMainThread = Thread.currentThread() == getMainThread();
                if ((info.exec == OnEvent.Thread.MAIN)) {
                    if (isMainThread) {
                        info.method.invoke(info.obj, remoteObj);
                    } else {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    info.method.invoke(info.obj, remoteObj);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                } else {
                    if (isMainThread) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    info.method.invoke(info.obj, remoteObj);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        threadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    info.method.invoke(info.obj, remoteObj);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private static Thread getMainThread() {
        return Looper.getMainLooper().getThread();
    }

    //TODO opt
    private Object copyToTarget(Object event, Class eventClass, Class remoteClass) {
        Field[] originFieldList = eventClass.getDeclaredFields();
        Field[] remoteFieldList = remoteClass.getDeclaredFields();
        try {
            Object remoteObj = remoteClass.newInstance();
            Map<String, Field> remoteFieldMap = new HashMap<>();
            for (Field field : remoteFieldList) {
                remoteFieldMap.put(field.getName(), field);
            }
            for (Field field : originFieldList) {
                Field remote = remoteFieldMap.get(field.getName());
                if (remote == null) {
                    //throw new RuntimeException(remoteClass + "has no filed! " + field.getName());
                    Log.e("ProtocolEventBus", remoteClass + "has no filed! " + field.getName());
                    continue;
                }
                remote.setAccessible(true);
                field.setAccessible(true);
                remote.set(remoteObj, field.get(event));
            }
            return remoteObj;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class OnEventClassInfo {
        public Class clazz;
        public Object obj;
        public Method method;
        public Class paramClazz;
        public int exec;

        public OnEventClassInfo(Class clazz, Object obj,
                                Method method, Class paramClazz, int exec) {
            this.clazz = clazz;
            this.method = method;
            this.obj = obj;
            this.paramClazz = paramClazz;
            this.exec = exec;
        }

    }

    private class BackgroundHandler extends HandlerThread {

        public BackgroundHandler(String name, int priority) {
            super(name, priority);
        }

    }


}
