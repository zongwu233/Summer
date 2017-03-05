package com.meiyou.framework.summer;

/**
 * Created by hxd on 16/3/24.
 */
@Deprecated
public interface FunctionMeta {
    //非必需, 只是更方便
    public String targetName();

    //全局唯一标识动作
    public String actionName();

    //非必需, 可以做参数类型校验用
    public Class<?>[] paramKeyTypes();

}
