package cn.penidea.alice.entity;

import lombok.Data;

/**
 * 功能:
 * 作者:Mr.Fokers
 * 日期：2022年09月28日 10:48
 */

@Data
public class Request<T> {
    private String action;
    private T params;
    private String echo;
}
