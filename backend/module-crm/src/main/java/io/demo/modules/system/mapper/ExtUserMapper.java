package io.demo.modules.system.mapper;


import io.demo.security.UserDTO;

public interface ExtUserMapper {
    UserDTO selectById(String id);
}
