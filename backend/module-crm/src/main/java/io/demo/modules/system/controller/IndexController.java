package io.demo.modules.system.controller;

import io.demo.modules.system.domain.UserKey;
import io.demo.modules.system.service.UserKeyService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 主页控制器类，处理访问根页面（"/"）和登录页面（"/login"）的请求。
 * <p>
 * 该控制器负责将请求转发到 `index.html` 页面。
 * </p>
 */
@Controller
public class IndexController {
    @Resource
    private UserKeyService userKeyService;

    /**
     * 处理根路径（"/"）的请求，并返回首页 `index.html` 页面。
     *
     * @return 返回首页的视图名称
     */
    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    /**
     * 处理登录页面（"/login"）的请求，并返回 `index.html` 页面。
     *
     * @return 返回登录页面的视图名称
     */
    @GetMapping(value = "/login")
    public String login() {
        return "index.html";
    }

    /**
     * 处理登录页面（"/login"）的请求，并返回 `index.html` 页面。
     *
     * @return 返回登录页面的视图名称
     */
    @GetMapping(value = "/test")
    public String test() {
        List<UserKey> admin = userKeyService.getUserKeysInfo("admin");


        return "index.html";
    }
}
