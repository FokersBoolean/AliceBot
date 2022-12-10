package cn.penidea.alice.entity;

import lombok.Data;

/**
 * 功能:
 * 作者:Mr.Fokers
 * 日期：2022年10月08日 16:49
 */

@Data
public class WsReturn<T> {
    private T data;
    private int retcode;
    private String status;
}
