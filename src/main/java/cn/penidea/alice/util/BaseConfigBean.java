package cn.penidea.alice.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseConfigBean {
    private String cqHttpWs;
    private HashMap<String, String> userList;
    private String atRobotCQ;
    private String wakeUpWord;
    private String robotName;
    private String standbyWord;
    private String promptUpWord;
}
