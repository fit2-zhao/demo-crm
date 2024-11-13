package io.demo.crm.services.system.mapper;

import io.demo.crm.services.system.domain.Schedule;
import io.demo.crm.services.system.domain.ScheduleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScheduleMapper {
    long countByExample(ScheduleExample example);

    int deleteByExample(ScheduleExample example);

    int deleteByPrimaryKey(String id);

    int insert(Schedule record);

    int insertSelective(Schedule record);

    List<Schedule> selectByExample(ScheduleExample example);

    Schedule selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") Schedule record, @Param("example") ScheduleExample example);

    int updateByExample(@Param("record") Schedule record, @Param("example") ScheduleExample example);

    int updateByPrimaryKeySelective(Schedule record);

    int updateByPrimaryKey(Schedule record);

    int batchInsert(@Param("list") List<Schedule> list);

    int batchInsertSelective(@Param("list") List<Schedule> list, @Param("selective") Schedule.Column ... selective);
}