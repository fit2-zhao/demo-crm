package io.demo.common.security.realm;


import io.demo.common.constants.UserSource;
import io.demo.common.util.LogUtils;
import io.demo.common.util.Translator;
import io.demo.modules.system.service.UserLoginService;
import io.demo.security.SessionConstants;
import io.demo.security.SessionUser;
import io.demo.security.SessionUtils;
import io.demo.security.UserDTO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;


/**
 * 自定义Realm 注入service 可能会导致在 service的aop 失效，例如@Transactional,
 * 解决方法：
 * <p>
 * 1. 这里改成注入mapper，这样mapper 中的事务失效<br/>
 * 2. 这里仍然注入service，在配置ShiroConfig 的时候不去set realm, 等到spring 初始化完成之后
 * set realm
 * </p>
 */
public class LocalRealm extends AuthorizingRealm {
    @Resource
    private UserLoginService userLoginService;

    @Override
    public String getName() {
        return "LOCAL";
    }

    /**
     * 角色认证
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        Session session = SecurityUtils.getSubject().getSession();
        String login = (String) session.getAttribute("authenticate");

        String userId = token.getUsername();
        String password = String.valueOf(token.getPassword());

        if (StringUtils.equals(login, UserSource.LOCAL.name())) {
            return loginLocalMode(userId, password);
        }

        UserDTO user = getUserWithOutAuthenticate(userId);
        userId = user.getId();
        SessionUser sessionUser = SessionUser.fromUser(user, (String) session.getId());
        session.setAttribute(SessionConstants.ATTR_USER, sessionUser);
        return new SimpleAuthenticationInfo(userId, password, getName());

    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {

        // TODO：增加自己的权限验证

        return super.isPermitted(principals, permission);
    }

    private UserDTO getUserWithOutAuthenticate(String userId) {
        UserDTO user = userLoginService.getUserDTO(userId);
        String msg;
        if (user == null) {
            user = userLoginService.getUserDTOByEmail(userId);
            if (user == null) {
                msg = "The user does not exist: " + userId;
                LogUtils.warn(msg);
                throw new UnknownAccountException(Translator.get("password_is_incorrect"));
            }
        }
        return user;
    }

    private AuthenticationInfo loginLocalMode(String userId, String password) {
        UserDTO user = userLoginService.getUserDTO(userId);
        String msg;
        if (user == null) {
            user = userLoginService.getUserDTOByEmail(userId);
            if (user == null) {
                msg = "The user does not exist: " + userId;
                LogUtils.warn(msg);
                throw new UnknownAccountException(Translator.get("password_is_incorrect"));
            }
            userId = user.getId();
        }
        // 密码验证
        if (!userLoginService.checkUserPassword(userId, password)) {
            throw new IncorrectCredentialsException(Translator.get("password_is_incorrect"));
        }
        SessionUser sessionUser = SessionUser.fromUser(user, SessionUtils.getSessionId());
        SessionUtils.putUser(sessionUser);
        return new SimpleAuthenticationInfo(userId, password, getName());
    }

}
