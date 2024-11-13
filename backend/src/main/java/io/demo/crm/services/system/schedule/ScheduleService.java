package io.demo.crm.services.system.schedule;

import io.demo.crm.common.exception.SystemException;
import io.demo.crm.services.system.constants.ApplicationNumScope;
import io.demo.crm.services.system.domain.Schedule;
import io.demo.crm.common.uid.IDGenerator;
import io.demo.crm.common.uid.NumGenerator;
import io.demo.crm.services.system.domain.ScheduleExample;
import io.demo.crm.services.system.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 定时任务服务类，负责调度任务的创建、编辑、删除及相关操作。
 * 主要功能包括定时任务的增、删、查及其与调度器的交互。
 *
 * @since 1.0
 */
@Transactional(rollbackFor = Exception.class)
public class ScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private ScheduleManager scheduleManager;

    /**
     * 添加新的定时任务。
     *
     * @param schedule 定时任务对象
     */
    public void addSchedule(Schedule schedule) {
        schedule.setId(IDGenerator.nextStr());
        schedule.setCreateTime(System.currentTimeMillis());
        schedule.setUpdateTime(System.currentTimeMillis());
        schedule.setNum(getNextNum(schedule.getProjectId()));
        scheduleMapper.insert(schedule);
    }

    /**
     * 获取下一个任务编号。
     *
     * @param projectId 项目 ID
     * @return 下一个任务编号
     */
    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.TASK);
    }

    /**
     * 根据任务 ID 获取定时任务。
     *
     * @param scheduleId 定时任务 ID
     * @return 定时任务对象
     */
    public Schedule getSchedule(String scheduleId) {
        return scheduleMapper.selectByPrimaryKey(scheduleId);
    }

    /**
     * 编辑定时任务信息。
     *
     * @param schedule 要更新的定时任务对象
     * @return 更新的记录数
     */
    public int editSchedule(Schedule schedule) {
        schedule.setUpdateTime(System.currentTimeMillis());
        return scheduleMapper.updateByPrimaryKeySelective(schedule);
    }

    /**
     * 根据资源 ID 和任务名查询定时任务。
     *
     * @param resourceId 资源 ID
     * @param job        任务名称
     * @return 匹配的定时任务对象，若没有则返回 null
     */
    public Schedule getScheduleByResource(String resourceId, String job) {
        ScheduleExample example = new ScheduleExample();
        example.createCriteria().andResourceIdEqualTo(resourceId).andJobEqualTo(job);
        List<Schedule> schedules = scheduleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(schedules)) {
            return schedules.getFirst();
        }
        return null;
    }

    /**
     * 根据资源 ID 删除任务，并移除调度器中的对应任务。
     *
     * @param scenarioId 资源 ID
     * @param jobKey     任务标识
     * @param triggerKey 触发器标识
     * @return 删除的记录数
     */
    public int deleteByResourceId(String scenarioId, JobKey jobKey, TriggerKey triggerKey) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(scenarioId);

        // 移除调度器中的任务
        scheduleManager.removeJob(jobKey, triggerKey);
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    /**
     * 根据资源 ID 删除任务并移除调度器中的对应任务。
     *
     * @param resourceId 资源 ID
     * @param group      任务组
     * @return 删除的记录数
     */
    public int deleteByResourceId(String resourceId, String group) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdEqualTo(resourceId);
        removeJob(resourceId, group);
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    /**
     * 根据资源 ID 列表删除任务，并移除调度器中的对应任务。
     *
     * @param resourceIds 资源 ID 列表
     * @param group       任务组
     * @return 删除的记录数
     */
    public int deleteByResourceIds(List<String> resourceIds, String group) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andResourceIdIn(resourceIds);
        for (String resourceId : resourceIds) {
            removeJob(resourceId, group);
        }
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    /**
     * 根据项目 ID 删除任务，并移除调度器中的对应任务。
     *
     * @param projectId 项目 ID
     * @return 删除的记录数
     */
    public int deleteByProjectId(String projectId) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andProjectIdEqualTo(projectId);
        List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
        schedules.forEach(item -> {
            removeJob(item.getKey(), item.getJob());
        });
        return scheduleMapper.deleteByExample(scheduleExample);
    }

    /**
     * 根据任务标识删除调度器中的任务。
     *
     * @param key 任务标识
     * @param job 任务名
     */
    private void removeJob(String key, String job) {
        scheduleManager.removeJob(new JobKey(key, job), new TriggerKey(key, job));
    }

    /**
     * 添加或更新 Cron 表达式定时任务。
     * 如果定时任务启用且 Cron 表达式有效，则添加或更新任务；否则，移除任务。
     *
     * @param request    定时任务请求对象
     * @param jobKey     任务标识
     * @param triggerKey 触发器标识
     * @param clazz      任务类
     */
    public void addOrUpdateCronJob(Schedule request, JobKey jobKey, TriggerKey triggerKey, Class clazz) {
        Boolean enable = request.getEnable();
        String cronExpression = request.getValue();
        if (BooleanUtils.isTrue(enable) && StringUtils.isNotBlank(cronExpression)) {
            try {
                // 添加或更新 Cron 表达式定时任务
                scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, clazz, cronExpression,
                        scheduleManager.getDefaultJobDataMap(request, cronExpression, request.getCreateUser()));
            } catch (SchedulerException e) {
                throw new SystemException("定时任务开启异常: " + e.getMessage());
            }
        } else {
            try {
                // 移除定时任务
                scheduleManager.removeJob(jobKey, triggerKey);
            } catch (Exception e) {
                throw new SystemException("定时任务关闭异常: " + e.getMessage());
            }
        }
    }
}
