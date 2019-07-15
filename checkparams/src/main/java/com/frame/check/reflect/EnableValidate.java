package com.frame.check.reflect;

import java.lang.annotation.*;

/**
 * @author:wangshuzheng
 * @date:2019/7/11 下午5:42
 * @des: 标识需要参数验证的接口方法
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface EnableValidate {

}
