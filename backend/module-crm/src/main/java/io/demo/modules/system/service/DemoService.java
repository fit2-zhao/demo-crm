package io.demo.modules.system.service;

import io.demo.aspectj.annotation.OperationLog;
import io.demo.aspectj.constants.LogModule;
import io.demo.aspectj.constants.LogType;
import io.demo.aspectj.context.OperationLogContext;
import io.demo.aspectj.dto.LogExtraDTO;
import io.demo.modules.system.domain.User;
import io.demo.mybatis.BaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

    @Resource
    private BaseMapper<User> userMapper;

    @OperationLog(
            module = LogModule.SYSTEM,
            type = LogType.ADD,
            operator = "{{#user.name}}",
            resourceId = "{{#user.id}}",
            success = "添加用户成功",
            extra = "{{#newUser}}"
    )
    public void addUser(User user) {
        // 添加用户

        // 添加日志上下文
        OperationLogContext.putVariable("newUser", LogExtraDTO.builder()
                .originalValue(null)
                .modifiedValue(user)
                .build());
    }

    @OperationLog(
            module = LogModule.SYSTEM,
            type = LogType.DELETE,
            operator = "{{#userId}}",
            resourceId = "{{#userId}}",
            success = "删除用户成功",
            extra = "{{#delUser}}"
    )
    public void deleteUser(String userId) {
        // 删除用户
        User user = userMapper.selectByPrimaryKey(userId);

        // 添加日志上下文
        OperationLogContext.putVariable("delUser", LogExtraDTO.builder()
                .originalValue(user)
                .modifiedValue(null)
                .build());
    }

    @OperationLog(
            module = LogModule.SYSTEM,
            type = LogType.UPDATE,
            operator = "{{#user.name}}",
            resourceId = "{{#user.id}}",
            success = "更新用户成功",
            extra = "{{#upUser}}"
    )
    @OperationLog(
            module = LogModule.SYSTEM,
            type = LogType.UPDATE,
            operator = "{{#user.name}}",
            resourceId = "{{#user.id}}",
            success = "更新用户成功",
            extra = "{{#upUser}}"
    )
    public void updateUser(User user) {
        // 更新用户
        User preUser = userMapper.selectByPrimaryKey(user.getId());

        // 添加日志上下文
        OperationLogContext.putVariable("upUser", LogExtraDTO.builder()
                .originalValue(preUser)
                .modifiedValue(user)
                .build());
    }

    public void getUser() {
        // 获取用户
    }
}
