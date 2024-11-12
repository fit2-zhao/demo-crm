package io.demo.crm.services.system.mapper.ext;

import io.demo.crm.common.dto.UserDTO;

public interface ExtUserMapper {
    UserDTO selectById(String id);
}
