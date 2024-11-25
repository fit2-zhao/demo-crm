package io.demo.crm.modules.system.mapper;

import io.demo.crm.common.dto.UserDTO;

public interface ExtUserMapper {
    UserDTO selectById(String id);
}
