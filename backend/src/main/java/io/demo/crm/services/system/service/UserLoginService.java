package io.demo.crm.services.system.service;

import io.demo.crm.services.system.constants.HttpMethodConstants;
import io.demo.crm.common.log.constants.LogConstants;
import io.demo.crm.services.system.constants.UserSource;
import io.demo.crm.common.exception.SystemException;
import io.demo.crm.common.util.CodingUtils;
import io.demo.crm.common.util.Translator;
import io.demo.crm.common.response.handler.ResultHolder;
import io.demo.crm.services.system.domain.User;
import io.demo.crm.services.system.domain.UserExample;
import io.demo.crm.common.dto.UserDTO;
import io.demo.crm.common.dto.LoginRequest;
import io.demo.crm.common.dto.SessionUser;
import io.demo.crm.common.log.constants.LogType;
import io.demo.crm.common.log.dto.LogDTO;
import io.demo.crm.common.log.service.LogService;
import io.demo.crm.services.system.mapper.ext.ExtUserMapper;
import io.demo.crm.services.system.mapper.UserMapper;
import io.demo.crm.common.util.SessionUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


@Service
@Transactional(rollbackFor = Exception.class)
public class UserLoginService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private LogService logService;
    @Resource
    private ExtUserMapper extUserMapper;

    public UserDTO getUserDTO(String userId) {
        UserDTO userDTO = extUserMapper.selectById(userId);
        if (userDTO == null) {
            return null;
        }
        if (BooleanUtils.isFalse(userDTO.getEnable())) {
            throw new DisabledAccountException();
        }
        return userDTO;
    }

    public UserDTO getUserDTOByEmail(String email, String... source) {
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(email);

        if (!CollectionUtils.isEmpty(Arrays.asList(source))) {
            criteria.andSourceIn(Arrays.asList(source));
        }

        List<User> users = userMapper.selectByExample(example);

        if (users == null || users.isEmpty()) {
            return null;
        }

        return getUserDTO(users.getFirst().getId());
    }


    public ResultHolder login(LoginRequest request) {
        String login = (String) SecurityUtils.getSubject().getSession().getAttribute("authenticate");
        String username = StringUtils.trim(request.getUsername());
        String password = StringUtils.EMPTY;
        if (!StringUtils.equals(login, UserSource.LDAP.name())) {
            password = StringUtils.trim(request.getPassword());
        }
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        Subject subject = SecurityUtils.getSubject();
        try {
            subject.login(token);
            saveLog(SessionUtils.getUserId(), HttpMethodConstants.POST.name(), "/login", "登录成功", LogType.LOGIN.name());
            if (subject.isAuthenticated()) {
                SessionUser sessionUser = SessionUtils.getUser();
                // 放入session中
                SessionUtils.putUser(sessionUser);
                return ResultHolder.success(sessionUser);
            } else {
                throw new SystemException(Translator.get("login_fail"));
            }
        } catch (ExcessiveAttemptsException e) {
            throw new ExcessiveAttemptsException(Translator.get("excessive_attempts"));
        } catch (LockedAccountException e) {
            throw new LockedAccountException(Translator.get("user_locked"));
        } catch (DisabledAccountException e) {
            throw new DisabledAccountException(Translator.get("user_has_been_disabled"));
        } catch (ExpiredCredentialsException e) {
            throw new ExpiredCredentialsException(Translator.get("user_expires"));
        } catch (AuthenticationException e) {
            throw new AuthenticationException(e.getMessage());
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(Translator.get("not_authorized") + e.getMessage());
        }
    }

    public boolean checkUserPassword(String userId, String password) {
        if (StringUtils.isBlank(userId)) {
            throw new SystemException(Translator.get("user_name_is_null"));
        }
        if (StringUtils.isBlank(password)) {
            throw new SystemException(Translator.get("password_is_null"));
        }
        UserExample example = new UserExample();
        example.createCriteria().andIdEqualTo(userId).andPasswordEqualTo(CodingUtils.md5(password));
        return userMapper.countByExample(example) > 0;
    }

    //保存日志
    public void saveLog(String userId, String method, String path, String content, String type) {
        User user = userMapper.selectByPrimaryKey(userId);
        LogDTO dto = new LogDTO(
                LogConstants.SYSTEM,
                LogConstants.SYSTEM,
                LogConstants.SYSTEM,
                userId,
                type,
                LogConstants.SYSTEM,
                StringUtils.join(user.getName(), StringUtils.EMPTY, content));
        dto.setMethod(method);
        dto.setPath(path);
        logService.add(dto);
    }
}
