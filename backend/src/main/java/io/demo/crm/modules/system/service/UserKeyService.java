package io.demo.crm.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.demo.crm.common.dto.builder.LogDTOBuilder;
import io.demo.crm.common.exception.SystemException;
import io.demo.crm.common.log.constants.LogConstants;
import io.demo.crm.common.log.constants.LogModule;
import io.demo.crm.common.log.constants.LogType;
import io.demo.crm.common.log.dto.LogDTO;
import io.demo.crm.common.log.service.LogService;
import io.demo.crm.common.uid.IDGenerator;
import io.demo.crm.common.util.JSON;
import io.demo.crm.common.util.Translator;
import io.demo.crm.modules.system.constants.HttpMethodConstants;
import io.demo.crm.modules.system.domain.UserKey;
import io.demo.crm.modules.system.mapper.UserKeyMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserKeyService {

    @Resource
    private UserKeyMapper userKeyMapper;

    @Resource
    private UserLoginService userLoginService;

    @Resource
    private LogService logService;

    /**
     * 获取指定用户的 API 密钥信息
     */
    public List<UserKey> getUserKeysInfo(String userId) {
        QueryWrapper<UserKey> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("create_user", userId);
        return userKeyMapper.selectList(queryWrapper);
    }

    /**
     * 为指定用户添加 API 密钥
     */
    public void addUserKey(String userId) {
        validateUserExistence(userId);

        List<UserKey> userKeysList = getUserKeysByUserId(userId);

        // 如果该用户已有 5 个密钥，抛出限制异常
        if (!CollectionUtils.isEmpty(userKeysList) && userKeysList.size() >= 5) {
            throw new SystemException(Translator.get("user_apikey_limit"));
        }

        UserKey userKey = generateUserKey(userId);
        userKeyMapper.insert(userKey);

        logApiKeyAction(userKey, LogType.ADD.name());
    }

    /**
     * 删除指定 ID 的 API 密钥
     */
    public void deleteUserKey(String id) {
        UserKey userKey = validateAndGetUserKey(id);
        userKeyMapper.deleteById(userKey.getId());
    }

    /**
     * 启用指定 ID 的 API 密钥
     */
    public void enableUserKey(String id) {
        UserKey userKey = validateAndGetUserKey(id);
        updateUserKeyStatus(userKey, true);
    }

    /**
     * 禁用指定 ID 的 API 密钥
     */
    public void disableUserKey(String id) {
        UserKey userKey = validateAndGetUserKey(id);
        updateUserKeyStatus(userKey, false);
    }

    /**
     * 根据 accessKey 获取 API 密钥信息
     */
    public UserKey getUserKey(String accessKey) {
        UserKey example = new UserKey();
        example.setAccessKey(accessKey);
        example.setEnable(true);
        QueryWrapper<UserKey> queryWrapper = new QueryWrapper<>(example);
        List<UserKey> userKeysList = userKeyMapper.selectList(queryWrapper);

        return CollectionUtils.isEmpty(userKeysList) ? null : userKeysList.getFirst();
    }

    /**
     * 校验 API 密钥是否存在
     */
    private UserKey validateAndGetUserKey(String id) {
        UserKey userKey = userKeyMapper.selectById(id);
        if (userKey == null) {
            throw new SystemException(Translator.get("api_key_not_exist"));
        }
        return userKey;
    }

    /**
     * 校验用户是否存在
     */
    private void validateUserExistence(String userId) {
        if (userLoginService.getUserDTO(userId) == null) {
            throw new SystemException(Translator.get("user_not_exist") + userId);
        }
    }

    /**
     * 获取指定用户的所有 API 密钥
     */
    private List<UserKey> getUserKeysByUserId(String userId) {
        UserKey example = new UserKey();
        example.setCreateUser(userId);

        QueryWrapper<UserKey> queryWrapper = new QueryWrapper<>(example);
        return userKeyMapper.selectList(queryWrapper);
    }

    /**
     * 生成新的 API 密钥
     */
    private UserKey generateUserKey(String userId) {
        UserKey userKey = new UserKey();
        userKey.setId(IDGenerator.nextStr());
        userKey.setCreateUser(userId);
        userKey.setEnable(true);
        userKey.setAccessKey(RandomStringUtils.randomAlphanumeric(16));
        userKey.setSecretKey(RandomStringUtils.randomAlphanumeric(16));
        userKey.setCreateTime(System.currentTimeMillis());
        userKey.setForever(true);
        return userKey;
    }

    /**
     * 更新 API 密钥的状态（启用/禁用）
     */
    private void updateUserKeyStatus(UserKey userKey, boolean enable) {
        userKey.setEnable(enable);
        userKeyMapper.updateById(userKey);
    }

    /**
     * 记录 API 密钥操作日志
     */
    private void logApiKeyAction(UserKey userKey, String actionType) {
        LogDTO logDTO = LogDTOBuilder.builder()
                .projectId(LogConstants.SYSTEM)
                .organizationId(LogConstants.SYSTEM)
                .type(actionType)
                .module(LogModule.PERSONAL_INFORMATION_APIKEY)
                .method(HttpMethodConstants.GET.name())
                .path("/user/api/key/add")
                .sourceId(userKey.getId())
                .content(userKey.getAccessKey())
                .originalValue(JSON.toJSONBytes(userKey))
                .build()
                .getLogDTO();
        logService.add(logDTO);
    }
}
