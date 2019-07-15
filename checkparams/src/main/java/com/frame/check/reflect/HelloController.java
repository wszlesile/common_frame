package com.frame.check.reflect;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class HelloController {
    @EnableValidate
    @RequestMapping("/hello")
    @ResponseBody
    public Map<String, Object> hello(TestField testField){
        Map<String,Object> result = new HashMap<>();
        if(testField.getPass() == null || testField.getPass().equals("")){
            result.put("msg","hello fail");
        }else {
            result.put("msg","hello success");
        }
        return result;
    }
    @ExceptionHandler(value = WrongArgsException.class)
    @ResponseBody
    public Object exceptionHandler(WrongArgsException e){
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("code",0);
        resultMap.put("msg",e.getErrMsg());
        return resultMap;
    }
}
