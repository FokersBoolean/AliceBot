package cn.penidea.alice;

import cn.penidea.alice.util.Constants;
import cn.penidea.alice.ws.Client;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 功能:
 * 作者:Mr.FoLio
 * 日期：2022年09月28日 9:49
 */

@Component
public class Start implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        if (!Client.connect(Constants.WS_URL)) {
            Client.reConnect();
        }
    }
}
