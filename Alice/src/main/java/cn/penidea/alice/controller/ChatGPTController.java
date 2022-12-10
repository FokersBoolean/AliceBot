package cn.penidea.alice.controller;

import cn.penidea.alice.service.impl.ChatGPTServiceImpl;
import cn.penidea.alice.util.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能:chatGPT 测试API接口
 * 作者:Mr.Fokers
 * 日期：2022年12月08日 8:59
 */

@RestController
@RequestMapping("/api")
public class ChatGPTController {

    @Autowired
    private ChatGPTServiceImpl chatGPTService;

    @RequestMapping(value = "/ask")
    public ResultBean askQuestion(String question) {
        return ResultBean.get(ResultBean.SUCCESS, "success", chatGPTService.askQuestion(question));
    }
}
