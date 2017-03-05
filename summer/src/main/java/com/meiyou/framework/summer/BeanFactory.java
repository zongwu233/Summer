package com.meiyou.framework.summer;

/**
 * Created by hxd on 16/3/24.
 */
public interface BeanFactory {
    public<T> Object getBean(Class<T> clazz);
}
