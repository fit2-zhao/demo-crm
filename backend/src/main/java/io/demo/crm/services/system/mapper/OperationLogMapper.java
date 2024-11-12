package io.demo.crm.services.system.mapper;

import io.demo.crm.services.system.domain.OperationLog;
import io.demo.crm.services.system.domain.OperationLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OperationLogMapper {
    long countByExample(OperationLogExample example);

    int deleteByExample(OperationLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OperationLog record);

    int insertSelective(OperationLog record);

    List<OperationLog> selectByExample(OperationLogExample example);

    OperationLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") OperationLog record, @Param("example") OperationLogExample example);

    int updateByExample(@Param("record") OperationLog record, @Param("example") OperationLogExample example);

    int updateByPrimaryKeySelective(OperationLog record);

    int updateByPrimaryKey(OperationLog record);

    int batchInsert(@Param("list") List<OperationLog> list);

    int batchInsertSelective(@Param("list") List<OperationLog> list, @Param("selective") OperationLog.Column ... selective);
}