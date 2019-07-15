package com.frame.check.reflect;

import lombok.Data;

/**
 * @author:wangshuzheng
 * @date:2019/7/15 下午2:24
 * @des:
 */
@Data
public class TestField {
    @RequestField(required = true)
    private String pass;
    @RequestField(required = true)
    private int passx;
}
