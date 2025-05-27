package com.rabbiter.fm.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer status_code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setStatus_code(1);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setStatus_code(0);
        result.setMsg(msg);
        return result;
    }
}