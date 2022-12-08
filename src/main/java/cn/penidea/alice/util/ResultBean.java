package cn.penidea.alice.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能:返回实体类
 * 作者:Mr.Fokers
 * 日期：2022年12月08日 9:01
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResultBean {
    private Integer code;
    private String msg;
    private Object data;

    public static final Integer SUCCESS = 200;

    public static final Integer FAIL = 400;

    public static ResultBean ok() {
        return new ResultBean(200, "success", null);
    }

    public static ResultBean ok(String msg) {
        return new ResultBean(SUCCESS, msg, null);
    }

    public static ResultBean ok(Object data) {
        return new ResultBean(SUCCESS, "success", data);
    }

    public static ResultBean ok(String msg, Object data) {
        return new ResultBean(SUCCESS, msg, data);
    }

    public static ResultBean fail() {
        return new ResultBean(FAIL, "error", null);
    }

    public static ResultBean fail(String msg) {
        return new ResultBean(FAIL, msg, null);
    }

    public static ResultBean fail(String msg, Object data) {
        return new ResultBean(FAIL, msg, data);
    }

    public static ResultBean get(Integer code, String msg, Object data) {
        return new ResultBean(code, msg, data);
    }
}
