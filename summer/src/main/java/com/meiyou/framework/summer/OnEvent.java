package com.meiyou.framework.summer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hxd on 16/6/20.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OnEvent {

    /**
     *
     * @return 唯一标示 事件
     */
    public String value();
    // multi dex has error cannot use enum
    //java.lang.IllegalAccessError: Class ref in pre-verified class resolved to unexpected implementation
    public static class Thread {
        public static final int MAIN=0;
        public static final int BACK_GROUND =1;
    }

    public int exec() default Thread.MAIN;
}
