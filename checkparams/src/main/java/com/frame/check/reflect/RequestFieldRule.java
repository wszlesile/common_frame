package com.frame.check.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author:wangshuzheng
 * @date:2019/7/11 下午5:36
 * @des: 参数验证规则
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestFieldRule {

    /**
     * 代表本规则的名称
     * @return
     */
    String ruleName();

    /**
     * 代表字段是否必传
     * @return
     */
    boolean required() default false;

    /**
     * 代表字段对应http请求中的键
     * @return
     */
    String field() default "";

    /**
     * 代表字段的中文标题
     * @return
     */
    String label() default "";

    /**
     * 属性的别名，跟field类似，不同的是field会让原始键失效，而alias同时启用多个键，当多个键存在时，取顺序在前的
     * @return
     */
    String[] alias() default {};

    /**
     * 非法值
     * @return
     */
    String[] illegals() default {};

}
