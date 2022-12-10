package cn.penidea.alice.entity;

import lombok.Data;

/**
 * 功能:
 * 作者:Mr.Fokers
 * 日期：2022年09月28日 10:51
 */

@Data
public class Params {
    private String message_type;
    private String user_id;
    private String group_id;
    private String message;
    private boolean auto_escape;
}
