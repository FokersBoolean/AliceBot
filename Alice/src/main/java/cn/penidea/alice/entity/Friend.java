package cn.penidea.alice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能:
 * 作者:Mr.Fokers
 * 日期：2022年10月08日 15:16
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friend {
    private String nickname;
    private String remark;
    private int user_id;
}
