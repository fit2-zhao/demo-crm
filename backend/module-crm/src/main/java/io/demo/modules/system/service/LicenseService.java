package io.demo.modules.system.service;

import io.demo.common.dto.LicenseDTO;

/**
 * 授权服务接口
 * <p>
 * 该接口提供了与授权相关的操作，包括验证、刷新、添加授权码等功能。
 * </p>
 */
public interface LicenseService {

    /**
     * 刷新当前的 License
     *
     * @return 刷新后的 License 信息
     */
    LicenseDTO refreshLicense();

    /**
     * 校验当前的 License 是否有效
     *
     * @return 校验结果，返回 License 信息
     */
    LicenseDTO validate();

    /**
     * 添加一个新的有效 License
     *
     * @param licenseCode 授权码
     * @param userId      用户 ID
     * @return 添加后的 License 信息
     */
    LicenseDTO addLicense(String licenseCode, String userId);

    /**
     * 获取加密后的授权码
     *
     * @param encrypt 加密字符串
     * @return 解密后的授权码
     */
    String getCode(String encrypt);
}
