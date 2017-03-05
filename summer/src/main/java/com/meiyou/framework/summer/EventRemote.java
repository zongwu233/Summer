package com.meiyou.framework.summer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hxd on 16/6/20.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
@Deprecated
public @interface EventRemote {
    /**
     * 映射事件
     * @return 唯一标示 事件
     */
    public String value();
}
