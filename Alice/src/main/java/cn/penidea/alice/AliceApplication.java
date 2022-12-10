package cn.penidea.alice;

import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.setting.dialect.Props;
import cn.penidea.alice.chatgpt.Chatbot;
import cn.penidea.alice.util.BaseConfigBean;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@SpringBootApplication
public class AliceApplication {
    @Value("${isFile}")
    private boolean isFile;
    @Value("${clientBaseConfig.admin}")
    private String adminQQ;
    @Value("${clientBaseConfig.robot}")
    private String robotQQ;
    @Value("${clientBaseConfig.wakeUpWord}")
    private String wakeUpWord;
    @Value("${clientBaseConfig.robotName}")
    public String robotName;
    @Value("${clientBaseConfig.standbyWord}")
    private String standbyWord;
    @Value("${clientBaseConfig.promptUpWord}")
    private String promptUpWord;
    @Value("${clientBaseConfig.standbyPrompt}")
    private String standbyPrompt;
    @Value("${clientBaseConfig.loadingWord}")
    private String loadingWord;
    @Value("${clientBaseConfig.isPublic}")
    private boolean isPublic;
    @Value("${chatGPT.email}")
    private String email;
    @Value("${chatGPT.password}")
    private String password;
    @Value("${chatGPT.sessionToken}")
    private String sessionToken;

    public static void main(String[] args) {
        SpringApplication.run(AliceApplication.class, args);
    }

    @SneakyThrows
    @Bean
    public BaseConfigBean baseConfig() {
        HashMap<String, String> userList = new HashMap<>();
        if (isFile) {
            String jarPath = getJarPath();
            File file = new File(jarPath + "config.properties");
            Properties properties = new Properties();
            properties.load(new java.io.FileReader(jarPath + "config.properties"));
            Props props = new Props(properties);
            adminQQ = props.getStr("admin");
            robotQQ = props.getStr("robot");
            wakeUpWord = props.getStr("wakeUpWord");
            robotName = props.getStr("robotName");
            standbyWord = props.getStr("standbyWord");
            promptUpWord = props.getStr("promptUpWord");
            standbyPrompt = props.getStr("standbyPrompt");
            loadingWord = props.getStr("loadingWord");
            isPublic = props.getBool("isPublic");
        }
        userList.put(adminQQ, "");
        userList.put("admin", adminQQ);
        return new BaseConfigBean(userList, "[CQ:at,qq=" + robotQQ + "]", wakeUpWord, robotName, standbyWord, promptUpWord, standbyPrompt, loadingWord, isPublic);
    }

    @SneakyThrows
    @Bean
    public Chatbot chatBot() {
        Map<String, String> params = new HashMap<>();
        if (isFile) {
            String jarPath = getJarPath();
            File file = new File(jarPath + "config.properties");
            if (!file.exists()) {
                String str = ResourceUtil.readUtf8Str("static/config.properties");
                FileWriter writer = new FileWriter(jarPath + "config.properties", "UTF-8");
                writer.write(str);
                System.out.println("config.properties已经生成在目录下，请前往配置参数");
                System.exit(0);
            } else {
                Properties properties = new Properties();
                properties.load(new java.io.FileReader(jarPath + "config.properties"));
                Props props = new Props(properties);
                email = props.getStr("email");
                password = props.getStr("password");
                sessionToken = props.getStr("sessionToken");
            }
        }
        params.put("email", email);
        params.put("password", password);
        params.put("session_token", sessionToken);
        return new Chatbot(params, null);
    }

    public String getJarPath() {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        String[] pathSplit = path.split("/");
        String jarName = pathSplit[pathSplit.length - 1];
        return path.replace(jarName, "");
    }
}
