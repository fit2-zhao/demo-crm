package io.demo.crm.services.system.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.demo.crm.common.util.JSON;
import io.demo.crm.common.util.LogUtils;
import io.demo.crm.services.system.domain.Schedule;
import io.demo.crm.services.system.mapper.ScheduleMapper;
import io.demo.crm.services.system.mapper.ext.ExtScheduleMapper;
import io.demo.crm.services.system.schedule.ScheduleManager;
import jakarta.annotation.Resource;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ExtScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private ExtScheduleMapper extScheduleMapper;

    // TODO: Add RESOURCE_TYPES
    private static final List<String> RESOURCE_TYPES = List.of(
    );

    public void startEnableSchedules() {
        long count = scheduleMapper.selectCount(new QueryWrapper<>());
        long pages = (long) Math.ceil(count / 100.0);

        for (int i = 0; i < pages; i++) {
            int start = i * 100;
            List<Schedule> schedules = extScheduleMapper.getScheduleByLimit(start, 100);
            doHandleSchedule(schedules);
        }
    }

    private void doHandleSchedule(List<Schedule> schedules) {
        schedules.forEach(schedule -> {
            try {
                if (schedule.getEnable()) {
                    if (RESOURCE_TYPES.contains(schedule.getResourceType())) {
                        removeJob(schedule); // 删除关闭的job
                    }
                    LogUtils.info("初始化任务：" + JSON.toJSONString(schedule));
                    scheduleManager.addOrUpdateCronJob(
                            new JobKey(schedule.getKey(), schedule.getJob()),
                            new TriggerKey(schedule.getKey(), schedule.getJob()),
                            Class.forName(schedule.getJob()),
                            schedule.getValue(),
                            scheduleManager.getDefaultJobDataMap(schedule, schedule.getValue(), schedule.getCreateUser())
                    );
                } else {
                    removeJob(schedule); // 删除关闭的job
                }
            } catch (ClassNotFoundException e) {
                LogUtils.error("任务类未找到：" + schedule.getJob(), e);
            } catch (Exception e) {
                LogUtils.error("初始化任务失败", e);
            }
        });
    }

    private void removeJob(Schedule schedule) {
        scheduleManager.removeJob(
                new JobKey(schedule.getKey(), schedule.getJob()),
                new TriggerKey(schedule.getKey(), schedule.getJob())
        );
    }
}
