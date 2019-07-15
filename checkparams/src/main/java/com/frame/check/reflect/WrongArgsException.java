package com.frame.check.reflect;

/**
 * @author:wangshuzheng
 * @date:2019/7/11 下午6:13
 * @des: 参数验证异常
 */
public class WrongArgsException extends Exception{
    /**
     * 验证失败信息 成功则为空
     */
    private String errMsg;
    public WrongArgsException(String errMsg){
        this.errMsg=errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
