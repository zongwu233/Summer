package com.meiyou.framework.summer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hxd on 16/3/28.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface FunctionShadow {
    /**
     *
     * @return  自动映射到标识的protocol方法上
     */
    public String value();
}
