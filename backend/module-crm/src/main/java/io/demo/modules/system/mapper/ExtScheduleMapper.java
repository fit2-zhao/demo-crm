package io.demo.modules.system.mapper;

import io.demo.modules.system.domain.Schedule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtScheduleMapper {

    List<Schedule> getScheduleByLimit(@Param("start") int start, @Param("limit") int limit);
}
