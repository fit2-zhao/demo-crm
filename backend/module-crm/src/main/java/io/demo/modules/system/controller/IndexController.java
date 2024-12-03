package io.demo.modules.system.controller;

import io.demo.modules.system.domain.User;
import io.demo.modules.system.service.DemoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 主页控制器类，处理访问根页面（"/"）和登录页面（"/login"）的请求。
 * <p>
 * 该控制器负责将请求转发到 `index.html` 页面。
 * </p>
 */
@Controller
public class IndexController {
    @Resource
    private DemoService demoService;

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

    // todo: 以下部分 DEMO 日志记录功能示例，后续删除

    @GetMapping(value = "/demo/add")
    public User add() {
        User user = new User();
        user.setId("1");
        user.setName("test");

        demoService.addUser(user);

        return user;
    }

    @GetMapping(value = "/demo/delete")
    public void del() {
        demoService.deleteUser("testUser");
    }

    @GetMapping(value = "/demo/update")
    public User update() {
        User user = new User();
        user.setId("1");
        user.setName("test");
        demoService.updateUser(user);
        return user;
    }

}
