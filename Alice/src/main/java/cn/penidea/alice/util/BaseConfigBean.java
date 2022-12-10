package cn.penidea.alice.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * 功能:基础配置类
 * 作者:Mr.Fokers
 * 日期：2022年12月09日 2:13
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BaseConfigBean {
    private HashMap<String, String> userList;
    private String atRobotCQ;
    private String wakeUpWord;
    private String robotName;
    private String standbyWord;
    private String promptUpWord;
    private String standbyPrompt;
    private String loadingWord;
    private boolean isPublic;
}
