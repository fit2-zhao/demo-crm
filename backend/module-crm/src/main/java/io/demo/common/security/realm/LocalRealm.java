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
 * Custom Realm that injects service. Injecting service may cause AOP in the service to fail, such as @Transactional.
 * Solutions:
 * <p>
 * 1. Inject mapper here, which will cause transactions in the mapper to fail.<br/>
 * 2. Still inject service here, and do not set realm when configuring ShiroConfig. Set realm after Spring initialization is complete.
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
     * Role authorization
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    /**
     * Login authentication
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
        // TODO: Add your own permission verification
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
        // Password verification
        if (!userLoginService.checkUserPassword(userId, password)) {
            throw new IncorrectCredentialsException(Translator.get("password_is_incorrect"));
        }
        SessionUser sessionUser = SessionUser.fromUser(user, SessionUtils.getSessionId());
        SessionUtils.putUser(sessionUser);
        return new SimpleAuthenticationInfo(userId, password, getName());
    }
}