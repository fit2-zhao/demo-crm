package io.demo.crm.services.system.controller;

import com.fasterxml.jackson.databind.node.TextNode;
import io.demo.crm.common.dto.LicenseDTO;
import io.demo.crm.services.system.service.LicenseService;
import io.demo.crm.common.util.CommonBeanFactory;
import io.demo.crm.common.util.SessionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 授权管理控制器类，负责处理与 License 相关的请求。
 * <p>
 * 该控制器包括 License 校验和添加有效 License 的操作。
 * </p>
 */
@RestController
@RequestMapping("/license")
@Tag(name = "系统设置-系统-授权管理")
public class LicenseController {

    /**
     * 校验 License 是否有效。
     *
     * @return 返回 LicenseDTO 对象，包含校验结果。
     */
    @GetMapping("/validate")
    @Operation(summary = "license 校验")
    public LicenseDTO validate() {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        if (licenseService != null) {
            return licenseService.validate();
        }
        return new LicenseDTO();
    }

    /**
     * 添加有效的 License。
     *
     * @param licenseCode 要添加的 License 代码。
     * @return 返回 LicenseDTO 对象，包含添加的 License 信息。
     */
    @PostMapping("/add")
    @Operation(summary = "添加有效的 License")
    public LicenseDTO addLicense(@RequestBody TextNode licenseCode) {
        LicenseService licenseService = CommonBeanFactory.getBean(LicenseService.class);
        if (licenseService != null) {
            return licenseService.addLicense(licenseCode.asText(), SessionUtils.getUserId());
        }
        return new LicenseDTO();
    }
}
