package com.frame.check.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author:wangshuzheng
 * @date:2019/7/11 下午5:31
 * @des: 用于修饰参数字段 请求Entity的属性 或者 controller方法入参
 */
@Target({ElementType.FIELD,ElementType.PARAMETER,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestField {

    /**
     * 表示是否是必须字段
     * @return
     */
    boolean required() default false;

    /**
     * 代表字段对应http请求中的键
     * @return
     */
    String field() default "";

    /**
     * 代表字段标题
     * @return
     */
    String label() default "";

    /**
     * 属性的别名，跟field类似，不同的是field会让原始键失效，而alias同时启用多个键，当多个键存在时，取顺序在前的
     * @return
     */
    String[] alias() default {};

    /**
     * 规则表
     * @return
     */
    RequestFieldRule[] rules() default {};

    /**
     * 默认规则
     * @return
     */
    String defaultRule() default "";

    /**
     * 非法值
     * @return
     */
    String[] illegals() default {};
}
