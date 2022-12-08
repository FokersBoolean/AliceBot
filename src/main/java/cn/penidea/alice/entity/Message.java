package cn.penidea.alice.entity;

import lombok.Data;

/**
 * 功能:
 * 作者:Mr.Fokers
 * 日期：2022年09月28日 10:34
 */
@Data
public class Message {
    private String post_type;
    private String meta_event_type;
    private String message_type;
    private Long time;
    private String self_id;
    private String sub_type;
    private String user_id;
    private String sender_id;
    private String group_id;
    private String target_id;
    private String message;
    private String raw_message;
    private Integer font;
    private String message_id;
    private Integer message_seq;
    private String anonymous;

}
