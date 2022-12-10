package cn.penidea.alice.chatgpt;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.gson.Gson;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Log4j2
public class Chatbot {
    private Map<String, String> config;
    private String conversationId;
    private String parentId;
    private Map<String, String> headers;

    private String conversationIdPrev;
    private String parentIdPrev;


    private final Gson gson = new Gson();

    public Chatbot(Map<String, String> config, String conversationId) {
        log.info("----------正在连接至OpenAPI-ChatGPT----------");
        this.config = config;
        this.conversationId = conversationId;
        this.parentId = UUID.randomUUID().toString();
        if (config.containsKey("session_token") || (config.containsKey("email")
                && config.containsKey("password"))) {
            refreshSession();
        }
    }

    public Chatbot(String sessionToken) {
        Map<String, String> config = new HashMap<>();
        config.put("session_token", sessionToken);
        this.parentId = UUID.randomUUID().toString();
        refreshSession();
    }

    // 重置对话 ID 和父 ID
    public void resetChat() {
        this.conversationId = null;
        this.parentId = UUID.randomUUID().toString();
    }


    // 刷新标题
    public void refreshHeaders() {
        if (!config.containsKey("Authorization")) {
            config.put("Authorization", "");
        } else if (config.get("Authorization") == null) {
            config.put("Authorization", "");
        }
        this.headers = new HashMap<String, String>() {{
            put("Host", "chat.openai.com");
            put("Accept", "text/event-stream");
            put("Authorization", "Bearer " + config.get("Authorization"));
            put("Content-Type", "application/json");
            put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1" +
                    ".15 (KHTML, like Gecko) " +
                    "Version/16.1 Safari/605.1.15");
            put("X-Openai-Assistant-App-Id", "");
            put("Connection", "close");
            put("Accept-Language", "en-US,en;q=0.9");
            put("Referer", "https://chat.openai.com/chat");
        }};

    }


    Map<String, Object> getChatStream(Map<String, Object> data) {
        String url = "https://chat.openai.com/backend-api/conversation";

        String body = HttpUtil.createPost(url)
                .headerMap(headers, true)
                .body(JSON.toJSONString(data), "application/json")
                .execute()
                .body();

        String message = "";
        Map<String, Object> chatData = new HashMap<>();
        for (String s : body.split("\n")) {
            if ((s == null) || "".equals(s)) {
                continue;
            }
            if (s.contains("data: [DONE]")) {
                continue;
            }
            String part = s.substring(5);
            JSONObject lineData = JSON.parseObject(part);
            try {
                JSONArray jsonArray = lineData.getJSONObject("message")
                        .getJSONObject("content")
                        .getJSONArray("parts");
                if (jsonArray.size() == 0) {
                    continue;
                }
                message = jsonArray.getString(0);

                conversationId = lineData.getString("conversation_id");
                parentId = (lineData.getJSONObject("message")).getString("id");

                chatData.put("message", message);
                chatData.put("conversation_id", conversationId);
                chatData.put("parent_id", parentId);
            } catch (Exception e) {
                log.error("getChatStream Exception: " + part);
                continue;
            }

        }
        return chatData;

    }

    // 获取文本形式的聊天响应
    public Map<String, Object> getChatText(Map<String, Object> data) {

        // 创建请求会话
        Session session = new Session();

        // 设置标题
        session.setHeaders(this.headers);

        // 设置多个cookie
        session.getCookies().put("__Secure-next-auth.session-token", config.get(
                "session_token"));
        session.getCookies().put("__Secure-next-auth.callback-url", "https://chat.openai.com/");

        // 设置代理
        if (config.get("proxy") != null && !config.get("proxy").equals("")) {
            Map<String, String> proxies = new HashMap<>();
            proxies.put("http", config.get("proxy"));
            proxies.put("https", config.get("proxy"));
            session.setProxies(proxies);
        }

        HttpResponse response = session.post2("https://chat.openai.com/backend-api/conversation",
                data);
        String body = response.body();

        String message = "";
        Map<String, Object> chatData = new HashMap<>();
        for (String s : body.split("\n")) {
            if ((s == null) || "".equals(s)) {
                continue;
            }
            if (s.contains("data: [DONE]")) {
                continue;
            }

            String part = s.substring(5);
            JSONObject lineData = JSON.parseObject(part);

            try {

                JSONArray jsonArray = lineData.getJSONObject("message")
                        .getJSONObject("content")
                        .getJSONArray("parts");

                if (jsonArray.size() == 0) {
                    continue;
                }
                message = jsonArray.getString(0);

                conversationId = lineData.getString("conversation_id");
                parentId = (lineData.getJSONObject("message")).getString("id");

                chatData.put("message", message);
                chatData.put("conversation_id", conversationId);
                chatData.put("parent_id", parentId);
            } catch (Exception e) {
                System.out.println("getChatStream Exception: " + part);
                //  e.printStackTrace();
                continue;
            }

        }
        return chatData;

    }

    public Map<String, Object> getChatResponse(String prompt, String output) {
        Map<String, Object> data = new HashMap<>();
        data.put("action", "next");
        data.put("conversation_id", this.conversationId);
        data.put("parent_message_id", this.parentId);
        data.put("model", "text-davinci-002-render");

        Map<String, Object> message = new HashMap<>();
        message.put("id", UUID.randomUUID().toString());
        message.put("role", "user");
        Map<String, Object> content = new HashMap<>();
        content.put("content_type", "text");
        content.put("parts", Collections.singletonList(prompt));
        message.put("content", content);
        data.put("messages", Collections.singletonList(message));

        this.conversationIdPrev = this.conversationId;
        this.parentIdPrev = this.parentId;

        if (output.equals("text")) {
            return this.getChatText(data);
        } else if (output.equals("stream")) {
            return this.getChatStream(data);
        } else {
            throw new RuntimeException("Output must be either 'text' or 'stream'");
        }
    }

    @SneakyThrows
    public void refreshSession() {
        if (!config.containsKey("session_token") && (!config.containsKey("email") ||
                !config.containsKey("password"))) {
            throw new RuntimeException("No tokens provided");
        } else if (config.containsKey("session_token")) {
            String sessionToken = config.get("session_token");
            if (sessionToken == null || sessionToken.equals("")) {
                throw new RuntimeException("No tokens provided");
            }
            Session session = new Session();

            // 设置代理
            if (config.get("proxy") != null && !config.get("proxy").equals("")) {
                Map<String, String> proxies = new HashMap<>();
                proxies.put("http", config.get("proxy"));
                proxies.put("https", config.get("proxy"));
                session.setProxies(proxies);
            }

            // 设置cookie
            session.getCookies().put("__Secure-next-auth.session-token", config.get(
                    "session_token"));

            String urlSession = "https://chat.openai.com/api/auth/session";
            HttpResponse response = session.get2(urlSession,
                    Collections.singletonMap(
                            "User-Agent",
                            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15" +
                                    " (KHTML," +
                                    " like Gecko) Version/16.1 Safari/605.1.15 "
                    ));

            try {
                String name = "__Secure-next-auth.session-token";
                String cookieValue = response.getCookieValue(name);
                config.put("session_token", cookieValue);

                String body = response.body();
                log.info("chatGptSessionToken: " + cookieValue);
                JSONObject responseObject = JSON.parseObject(body);

                String accessToken = responseObject.getString("accessToken");
                log.info("chatGptAccessToken: " + accessToken);

                config.put("Authorization", accessToken);
                log.info("------------ChatGPT API连接成功--------------");
                this.refreshHeaders();
            } catch (Exception e) {
                log.error("刷新会话时出错");
                throw new Exception("Error refreshing session", e);
            }
        } else if (config.containsKey("email") && config.containsKey("password")) {
            try {
                this.login(config.get("email"), config.get("password"));
            } catch (Exception e) {
                log.error("刷新会话时出错: " + e.getMessage());
                throw e;
            }
        } else {
            throw new RuntimeException("没有提供代币");
        }
    }


    public void login(String email, String password) {
        System.out.println("Logging in...");
        boolean useProxy = false;
        String proxy = null;
        if (config.containsKey("proxy")) {
            if (!config.get("proxy").equals("")) {
                useProxy = true;
                proxy = config.get("proxy");
            }
        }
        OpenAIAuth auth = new OpenAIAuth(email, password, useProxy, proxy);
        try {
            auth.begin();
        } catch (Exception e) {
            //如果带有 e 的 RuntimeException 作为“检测到的验证码”失败
            if (e.getMessage().equals("Captcha detected")) {
                System.out.println("Captcha not supported. Use session tokens instead.");
                throw new RuntimeException("Captcha detected", e);
            }
            throw new RuntimeException("Error logging in", e);
        }
        if (auth.getAccessToken() != null) {
            config.put("Authorization", auth.getAccessToken());
            if (auth.getSessionToken() != null) {
                config.put("session_token", auth.getSessionToken());
            } else {
                String possibleTokens = auth.getSession().getCookies().get("__Secure-next-auth" +
                        ".session-token");
                if (possibleTokens != null) {
                    if (possibleTokens.length() > 1) {
                        config.put("session_token", possibleTokens);
                    } else {
                        try {
                            config.put("session_token", possibleTokens);
                        } catch (Exception e) {
                            throw new RuntimeException("Error logging in", e);
                        }
                    }
                }
            }
            this.refreshHeaders();
        } else {
            throw new RuntimeException("Error logging in");
        }
    }

    public void rollbackConversation() {
        this.conversationId = this.conversationIdPrev;
        this.parentId = this.parentIdPrev;
    }

    @SneakyThrows
    public static JSONObject resJson(Response response) {
        JSONObject responseObject = null;
        String text = response.body().string();
        try {
            response.body().close();
            responseObject = JSON.parseObject(text);
        } catch (Exception e) {
            log.error("json err, body: " + text);
            throw new RuntimeException(e);
        }

        return responseObject;
    }

}
