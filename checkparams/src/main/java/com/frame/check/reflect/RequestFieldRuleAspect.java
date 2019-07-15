package com.frame.check.reflect;

import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author:wangshuzheng
 * @date:2019/7/11 下午5:42
 * @des: 参数规则解析类
 */
@Aspect
@Order(3)
@Service
public class RequestFieldRuleAspect {

    private static Logger log = LoggerFactory.getLogger(RequestFieldRuleAspect.class);

    @Pointcut("@annotation(com.frame.check.reflect.EnableValidate)")
    public void enableValidatePointcut() {
    }
    /**
     * 参数验证逻辑
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Around("enableValidatePointcut()")
    public Object interceptor(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法完整名称
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        methodName = className + "." + methodName + "()";
        System.out.println(methodName);
        // 获取request对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        // 参数规则验证
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        Method method =  signature.getMethod();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ParameterWrapper parameterWrapper = new ParameterWrapper(parameter);
            parameterWrapper.setName(parameterNames[i]);
            build(request,parameterWrapper);
        }

        // 调用handler方法
        return joinPoint.proceed(joinPoint.getArgs());
    }

    /**
     * 重写 java.lang.reflect.Parameter
     * 解决 '编译器为了压缩 .class 大小，压缩了参数名，默认用 argN, N代表方法参数列表下标'
     */
    @Data
    private static class ParameterWrapper implements AnnotatedElement{
        public  ParameterWrapper(Parameter parameter){
            this.parameter = parameter;
        }
        private Parameter parameter;
        String name;

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return parameter.getAnnotation(annotationClass);
        }

        @Override
        public Annotation[] getAnnotations() {
            return parameter.getAnnotations();
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return parameter.getDeclaredAnnotations();
        }
        public Class<?> getType() {
            return parameter.getType();
        }
    }
    private static void build(HttpServletRequest request, ParameterWrapper parameter) throws Exception {
        RequestField erf = parameter.getAnnotation(RequestField.class);
        String fieldName = parameter.getName();
        Class<?> clazz = parameter.getType();

        // 判断是不是原生类型
        boolean isBtype = CheckBtype(parameter.getType());

        if(isBtype){
            if(erf == null){
                throw new WrongArgsException(clazz.getName() + "没有指定验证规则，无法验证");
            }
            // 验证原生类型
            RuleField rule = new RuleField.Builder(clazz,erf).fieldName(fieldName).build();
            _build(request,rule);
        }else{
            /*Object obj;
            try {
                Constructor<?> ctor = clazz.getConstructor();
                obj = ctor.newInstance();
            } catch (Exception e) {
                log.error("param class must have a constructor", e);
                throw new WrongArgsException(clazz.getName() + "没有构造函数");
            }*/

            // 验证Entity 默认获取两级父类
            Field[] fields = getFileds(clazz, 2);
            // 验证自定义类型
            for (Field field : fields) {
                RequestField erfx = field.getAnnotation(RequestField.class);
                RuleField rule = new RuleField.Builder(clazz,erfx).fieldName(field.getName()).build();
                _build(request,rule);
            }
        }


    }
    /**
     * @author wangshuzheng
     * @date 2019/7/12 下午5:09
     * des 判断class是不是原生类型
    */
    private static boolean CheckBtype(Class<?> clazz) {
        if (clazz.equals(char.class)) {
             return true;
        } else if (clazz.equals(String.class)) {
            return true;
        } else if (clazz.equals(Byte.class) || clazz.equals(byte.class)) {
            return true;
        } else if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
            return true;
        } else if (clazz.equals(Long.class) || clazz.equals(long.class)) {
            return true;
        } else if (clazz.equals(Float.class) || clazz.equals(float.class)) {
            return true;
        } else if (clazz.equals(Double.class) || clazz.equals(Double.class)) {
            return true;
        }
        return false;
    }

    /**
     * 验证规则属性静态内部类
    */
    @Data
    private static class RuleField {
        Type type;
        String fieldName;
        RequestField erf;
        private RuleField(Builder builder){
            this.type = builder.type;
            this.fieldName = builder.fieldName;
            this.erf = builder.erf;
        }
        private static class Builder{
            private Type type;
            private String fieldName;

            private RequestField erf;

            public Builder(Type type,RequestField erf){
                 this.type = type;
                 this.erf = erf;
            }
            public Builder fieldName(String fieldName){
                this.fieldName = fieldName;
                return this;
            }

            public RuleField build(){
                return new RuleField(this);
            }
        }
    }
    private static void _build(HttpServletRequest request,RuleField field) throws Exception {
        Type type = field.getType();
        String fieldName = field.getFieldName();
        boolean required = false;
        String label = "";
        String[] alias = null;
        String[]  illegals = null;

        RequestField erf = field.getErf();
        if(erf != null){

            // 外层定义，会被具体rule覆盖
            required = erf.required();
            label = erf.label();
            if (!erf.field().trim().isEmpty()) {
                fieldName = erf.field();
            }
            if (erf.alias().length > 0) {
                alias = erf.alias();
            }
            illegals = erf.illegals();
            // 指定了某一个规则
            RequestFieldRule[] rules = erf.rules();
            if (rules != null && rules.length != 0) {
                RequestFieldRule rule = null;
                if (erf.defaultRule().equals("")) {
                    // 没有指定默认规则，默认规则表第一个规则
                    rule = rules[0];
                } else {
                    // 指定了默认规则，选择默认规则
                    String defaultRule = erf.defaultRule();
                    for (RequestFieldRule rulex : rules) {
                        if (rule.ruleName().equals(defaultRule)) {
                            rule = rulex;
                            break;
                        }
                    }
                }
                if (rule == null) {
                    throw new WrongArgsException("规则表没有找到默认规则信息，指定了一个以上规则表，必须指定默认规则");
                }
                required = rule.required();
                label = rule.label();
                if (!rule.field().trim().isEmpty()) {
                    fieldName = rule.field();
                }
                if (rule.alias().length > 0) {
                    alias = rule.alias();
                }
                illegals = erf.illegals();
            }
        }

        if (label.isEmpty()) {
            label = fieldName;
        }

        if (type.equals(List.class)) {
            // List类型
            String[] v = getParamsFromRequest(request, fieldName, alias);
            // 兼容用key=1,2,3的情况
            if (v == null || (v.length == 1 && v[0].contains(","))) {
                String vs = request.getParameter(fieldName);
                if (vs != null) {
                    v = vs.split(",");
                }
                if (v == null || v.length == 0) {
                    v = null;
                }
            }

            if (required && v == null) {
                // 字段是必须 且 字段值不存在时 抛出异常
                throw new WrongArgsException("需要参数[" + label + "]");
            }
            boolean isJoint = Collections.disjoint(Arrays.asList(illegals),Arrays.asList(v));
            if(isJoint){
                throw new WrongArgsException("参数[" + label + "]值不合法");
            }
        } else {
            // 从请求中获取值
            String v = getParamFromRequest(request, fieldName, alias);
            if (required && (v == null || v.isEmpty())) {
                // 字段是必须 且 字段值不存在时 抛出异常
                throw new WrongArgsException("需要参数[" + label + "]");
            }
            if(illegals !=null && Arrays.stream(illegals).anyMatch(illegal->illegal.equals(v))) {
                throw new WrongArgsException("参数[" + label + "]值不合法");
            }
        }
    }

    // 获取类的属性，包括父类；注意，limit包括子类

    /**
     * 获取类的属性
     * 比如从左到右分别是子类到父类: C(f4) -> B(f2,f3) -> A(f1)
     * 调用：getFields(C.class, limit)
     * limit=1：获取到属性[f4]
     * limit=2: 获取到属性[f4,f2,f3]
     * limit=3: 获取的属性[f4,f2,f3,f1]
     *
     * @param clazz 类型
     * @param limit 遍历的层级
     * @param <T>
     * @return
     */
    private static <T> Field[] getFileds(Class<T> clazz, int limit) {
        if (limit < 1) {
            limit = 1;
        }
        List<Field> fields = new ArrayList<Field>();
        int count = 0;
        Class<? super T> o = clazz;
        while (o != null && o != Object.class) {
            if (count >= limit) {
                break;
            }
            count++;
            Field[] fs = o.getDeclaredFields();
            Collections.addAll(fields, fs);
            o = o.getSuperclass();
        }

        Field[] fs = new Field[fields.size()];
        return fields.toArray(fs);
    }

    private static String getParamFromRequest(HttpServletRequest request, String fieldName, String[] alias) {
        String v = request.getParameter(fieldName);
        if (v == null && alias != null && alias.length > 0) {
            // 尝试根据别名获取
            for (int i = 0; i < alias.length; i++) {
                v = request.getParameter(alias[i]);
                if (v != null) {
                    return v;
                }
            }
        }

        return v;
    }

    private static String[] getParamsFromRequest(HttpServletRequest request, String fieldName, String[] alias) {
        String[] v = request.getParameterValues(fieldName);
        if (v == null && alias != null && alias.length > 0) {
            // 尝试根据别名获取
            for (int i = 0; i < alias.length; i++) {
                v = request.getParameterValues(alias[i]);
                if (v != null) {
                    return v;
                }
            }
        }

        return v;
    }

}
