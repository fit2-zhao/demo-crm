package io.demo.crm.services.system.mapper;

import io.demo.crm.services.system.domain.User;
import io.demo.crm.services.system.domain.UserExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {
    long countByExample(UserExample example);

    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(String id);

    int insert(User record);

    int insertSelective(User record);

    List<User> selectByExample(UserExample example);

    User selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") User record, @Param("example") UserExample example);

    int updateByExample(@Param("record") User record, @Param("example") UserExample example);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int batchInsert(@Param("list") List<User> list);

    int batchInsertSelective(@Param("list") List<User> list, @Param("selective") User.Column ... selective);
}