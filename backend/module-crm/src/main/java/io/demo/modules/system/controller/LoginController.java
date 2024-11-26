package io.demo.modules.system.controller;

import io.demo.aspectj.constants.LogType;
import io.demo.common.constants.HttpMethodConstants;
import io.demo.common.constants.UserSource;
import io.demo.common.request.LoginRequest;
import io.demo.common.exception.SystemException;
import io.demo.common.response.handler.ResultHolder;
import io.demo.common.response.result.MsHttpResultCode;
import io.demo.common.util.Translator;
import io.demo.common.util.rsa.RsaKey;
import io.demo.common.util.rsa.RsaUtils;
import io.demo.modules.system.service.UserLoginService;
import io.demo.security.SessionUser;
import io.demo.security.SessionUtils;
import io.demo.security.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 登录控制器，负责处理用户登录、校验和退出操作。
 * <p>
 * 该控制器包含检查是否已登录、获取公钥、用户登录和退出登录功能。
 * </p>
 */
@RestController
@RequestMapping
@Tag(name = "登录")
public class LoginController {

    @Resource
    private UserLoginService userLoginService;

    /**
     * 检查用户是否已登录。
     *
     * @param response HTTP 响应对象。
     * @return 返回用户会话信息，未登录则返回 401 错误。
     */
    @GetMapping(value = "/is-login")
    @Operation(summary = "是否登录")
    public ResultHolder isLogin(HttpServletResponse response) {
        SessionUser user = SessionUtils.getUser();
        if (user != null) {
            UserDTO userDTO = userLoginService.getUserDTO(user.getId());
            SessionUser sessionUser = SessionUser.fromUser(userDTO, SessionUtils.getSessionId());
            SessionUtils.putUser(sessionUser);
            return ResultHolder.success(sessionUser);
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return ResultHolder.error(MsHttpResultCode.UNAUTHORIZED.getCode(), null);
    }

    /**
     * 获取 RSA 公钥。
     *
     * @return 返回 RSA 公钥。
     * @throws Exception 可能抛出的异常。
     */
    @GetMapping(value = "/get-key")
    @Operation(summary = "获取公钥")
    public ResultHolder getKey() throws Exception {
        RsaKey rsaKey = RsaUtils.getRsaKey();
        return ResultHolder.success(rsaKey.getPublicKey());
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求对象，包含用户名和密码。
     * @return 登录结果。
     * @throws SystemException 如果已登录且当前用户与请求用户名不同，抛出异常。
     */
    @PostMapping(value = "/login")
    @Operation(summary = "登录")
    public ResultHolder login(@Validated @RequestBody LoginRequest request) {
        SessionUser sessionUser = SessionUtils.getUser();
        if (sessionUser != null) {
            // 如果当前用户已登录且用户名与请求用户名不匹配，抛出异常
            if (!StringUtils.equals(sessionUser.getId(), request.getUsername())) {
                throw new SystemException(Translator.get("please_logout_current_user"));
            }
        }
        // 设置认证方式为 LOCAL
        SecurityUtils.getSubject().getSession().setAttribute("authenticate", UserSource.LOCAL.name());
        return userLoginService.login(request);
    }

    /**
     * 退出登录。
     *
     * @return 返回退出成功信息。
     */
    @GetMapping(value = "/signout")
    @Operation(summary = "退出登录")
    public ResultHolder logout() {
        if (SessionUtils.getUser() == null) {
            return ResultHolder.success("logout success");
        }
        // 记录日志
        userLoginService.saveLog(SessionUtils.getUserId(), HttpMethodConstants.GET.name(), "/signout", "登出成功", LogType.LOGOUT.name());
        // 退出当前会话
        SecurityUtils.getSubject().logout();
        return ResultHolder.success("logout success");
    }
}
