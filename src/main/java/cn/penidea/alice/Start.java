package cn.penidea.alice;

import cn.penidea.alice.util.BaseConfigBean;
import cn.penidea.alice.ws.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 功能:
 * 作者:Mr.Fokers
 * 日期：2022年09月28日 9:49
 */

@Component
public class Start implements CommandLineRunner {
    private static BaseConfigBean baseConfigBean;
    @Autowired
    public void setBaseConfigBean(BaseConfigBean baseConfigBean) {
        Start.baseConfigBean = baseConfigBean;
    }
    @Override
    public void run(String... args) throws Exception {
        System.out.println("连接CQ-Http的地址: " + baseConfigBean.getCqHttpWs());
        if (!Client.connect(baseConfigBean.getCqHttpWs())) {
            Client.reConnect();
        }
    }
}
