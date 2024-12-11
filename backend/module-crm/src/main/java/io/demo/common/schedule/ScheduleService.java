package io.demo.common.schedule;

import io.demo.common.exception.GenericException;
import io.demo.mybatis.BaseMapper;
import io.demo.common.constants.ApplicationNumScope;
import io.demo.modules.system.domain.Schedule;
import io.demo.common.uid.IDGenerator;
import io.demo.common.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.transaction.annotation.Transactional;

/**
 * Schedule service class, responsible for creating, editing, deleting, and related operations of scheduled tasks.
 * Main functions include adding, deleting, querying scheduled tasks, and interacting with the scheduler.
 *
 * @since 1.0
 */
@Transactional(rollbackFor = Exception.class)
public class ScheduleService {

    @Resource
    private BaseMapper<Schedule> scheduleMapper;

    @Resource
    private ScheduleManager scheduleManager;

    /**
     * Adds a new scheduled task.
     *
     * @param schedule Scheduled task object
     */
    public void addSchedule(Schedule schedule) {
        schedule.setId(IDGenerator.nextStr());
        schedule.setCreateTime(System.currentTimeMillis());
        schedule.setUpdateTime(System.currentTimeMillis());
        schedule.setNum(getNextNum(schedule.getProjectId()));
        scheduleMapper.insert(schedule);
    }

    /**
     * Gets the next task number.
     *
     * @param projectId Project ID
     * @return The next task number
     */
    public long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.TASK);
    }

    /**
     * Gets the scheduled task by task ID.
     *
     * @param scheduleId Scheduled task ID
     * @return Scheduled task object
     */
    public Schedule getSchedule(String scheduleId) {
        return scheduleMapper.selectByPrimaryKey(scheduleId);
    }

    /**
     * Edits the scheduled task information.
     *
     * @param schedule The scheduled task object to be updated
     * @return The number of updated records
     */
    public int editSchedule(Schedule schedule) {
        schedule.setUpdateTime(System.currentTimeMillis());
        return scheduleMapper.update(schedule);
    }

    /**
     * Deletes the task from the scheduler by task identifier.
     *
     * @param key Task identifier
     * @param job Task name
     */
    private void removeJob(String key, String job) {
        scheduleManager.removeJob(new JobKey(key, job), new TriggerKey(key, job));
    }

    /**
     * Adds or updates a Cron expression scheduled task.
     * If the scheduled task is enabled and the Cron expression is valid, the task is added or updated; otherwise, the task is removed.
     *
     * @param request    Scheduled task request object
     * @param jobKey     Task identifier
     * @param triggerKey Trigger identifier
     * @param clazz      Task class
     */
    public void addOrUpdateCronJob(Schedule request, JobKey jobKey, TriggerKey triggerKey, Class clazz) {
        Boolean enable = request.getEnable();
        String cronExpression = request.getValue();
        if (BooleanUtils.isTrue(enable) && StringUtils.isNotBlank(cronExpression)) {
            try {
                // Add or update Cron expression scheduled task
                scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, clazz, cronExpression,
                        scheduleManager.getDefaultJobDataMap(request, cronExpression, request.getCreateUser()));
            } catch (SchedulerException e) {
                throw new GenericException("Exception occurred while enabling the scheduled task: " + e.getMessage());
            }
        } else {
            try {
                // Remove the scheduled task
                scheduleManager.removeJob(jobKey, triggerKey);
            } catch (Exception e) {
                throw new GenericException("Exception occurred while disabling the scheduled task: " + e.getMessage());
            }
        }
    }
}