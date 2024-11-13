package io.demo.crm.services.system.mapper.ext;

import io.demo.crm.services.system.domain.Schedule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtScheduleMapper {

    List<Schedule> getScheduleByLimit(@Param("start") int start, @Param("limit") int limit);
}
