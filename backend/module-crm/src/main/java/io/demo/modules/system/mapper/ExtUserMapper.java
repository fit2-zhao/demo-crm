package io.demo.modules.system.mapper;


import io.demo.common.dto.UserDTO;

public interface ExtUserMapper {
    UserDTO selectById(String id);
}
