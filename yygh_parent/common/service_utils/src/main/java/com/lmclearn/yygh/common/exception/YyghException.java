package com.lmclearn.yygh.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义异常
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YyghException extends RuntimeException{

    //状态码
    private Integer code;

    //异常信息
    private String msg;
}
