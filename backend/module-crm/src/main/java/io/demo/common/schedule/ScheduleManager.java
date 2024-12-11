package io.demo.common.schedule;

import io.demo.common.exception.GenericException;
import io.demo.common.util.LogUtils;
import io.demo.modules.system.domain.Schedule;
import jakarta.annotation.Resource;
import org.quartz.*;

/**
 * Schedule Manager, used to manage the addition, modification, and deletion of scheduled tasks.
 * Provides support for simple tasks and Cron expression tasks.
 * <p>
 * Main functions include:
 * <ul>
 *   <li>Adding and deleting scheduled tasks</li>
 *   <li>Modifying Cron expressions</li>
 *   <li>Starting and stopping the scheduler</li>
 * </ul>
 * </p>
 *
 * @since 1.0
 */
public class ScheduleManager {

    @Resource
    private Scheduler scheduler;

    /**
     * Adds a simple scheduled task.
     *
     * @param jobKey       Job identifier
     * @param triggerKey   Trigger identifier
     * @param cls          Job class
     * @param repeatIntervalTime Task repeat interval time (in hours)
     * @param jobDataMap   Job data
     * @throws SchedulerException If scheduling fails
     */
    public void addSimpleJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> cls, int repeatIntervalTime, JobDataMap jobDataMap)
            throws SchedulerException {

        JobBuilder jobBuilder = JobBuilder.newJob(cls).withIdentity(jobKey);
        if (jobDataMap != null) {
            jobBuilder.usingJobData(jobDataMap);
        }

        SimpleTrigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(repeatIntervalTime).repeatForever())
                .startNow().build();

        scheduler.scheduleJob(jobBuilder.build(), trigger);
    }

    /**
     * Adds a Cron expression scheduled task.
     *
     * @param jobKey       Job identifier
     * @param triggerKey   Trigger identifier
     * @param jobClass     Job class
     * @param cron         Cron expression
     * @param jobDataMap   Job data
     */
    public void addCronJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> jobClass, String cron, JobDataMap jobDataMap) {
        try {
            LogUtils.info("addCronJob: " + triggerKey.getName() + "," + triggerKey.getGroup());
            JobBuilder jobBuilder = JobBuilder.newJob(jobClass).withIdentity(jobKey);
            if (jobDataMap != null) {
                jobBuilder.usingJobData(jobDataMap);
            }

            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            triggerBuilder.withIdentity(triggerKey);
            triggerBuilder.startNow();
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            CronTrigger trigger = (CronTrigger) triggerBuilder.build();
            scheduler.scheduleJob(jobBuilder.build(), trigger);

        } catch (Exception e) {
            LogUtils.error(e);
            throw new GenericException("Scheduled task configuration exception: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a Cron expression scheduled task (without JobDataMap).
     *
     * @param jobKey     Job identifier
     * @param triggerKey Trigger identifier
     * @param jobClass   Job class
     * @param cron       Cron expression
     */
    public void addCronJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> jobClass, String cron) {
        addCronJob(jobKey, triggerKey, jobClass, cron, null);
    }

    /**
     * Modifies the Cron expression of an existing Cron trigger.
     *
     * @param triggerKey Trigger identifier
     * @param cron       New Cron expression
     * @throws SchedulerException If modification fails
     */
    public void modifyCronJobTime(TriggerKey triggerKey, String cron) throws SchedulerException {

        LogUtils.info("modifyCronJobTime: " + triggerKey.getName() + "," + triggerKey.getGroup());
        try {
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return;
            }

            String oldTime = trigger.getCronExpression();
            if (!oldTime.equalsIgnoreCase(cron)) {
                // Modify the Cron expression of the trigger
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
                triggerBuilder.withIdentity(triggerKey);
                triggerBuilder.startNow();
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
                trigger = (CronTrigger) triggerBuilder.build();
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to modify Cron expression", e);
        }
    }

    /**
     * Deletes the specified job and trigger.
     *
     * @param jobKey     Job identifier
     * @param triggerKey Trigger identifier
     */
    public void removeJob(JobKey jobKey, TriggerKey triggerKey) {
        try {
            LogUtils.info("RemoveJob: " + jobKey.getName() + "," + jobKey.getGroup());
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(jobKey);
        } catch (Exception e) {
            LogUtils.error("Failed to delete job", e);
            throw new RuntimeException("Failed to delete job", e);
        }
    }

    /**
     * Starts the scheduler.
     *
     * @param schedule Scheduler
     */
    public static void startJobs(Scheduler schedule) {
        try {
            schedule.start();
        } catch (Exception e) {
            LogUtils.error("Failed to start scheduler", e);
            throw new RuntimeException("Failed to start scheduler", e);
        }
    }

    /**
     * Shuts down the scheduler.
     *
     * @param schedule Scheduler
     */
    public void shutdownJobs(Scheduler schedule) {
        try {
            if (!schedule.isShutdown()) {
                schedule.shutdown();
            }
        } catch (Exception e) {
            LogUtils.error("Failed to shut down scheduler", e);
            throw new RuntimeException("Failed to shut down scheduler", e);
        }
    }

    /**
     * Adds or updates a Cron scheduled task.
     * If the trigger already exists, its Cron expression is modified; otherwise, a new Cron scheduled task is added.
     *
     * @param jobKey     Job identifier
     * @param triggerKey Trigger identifier
     * @param jobClass   Job class
     * @param cron       Cron expression
     * @param jobDataMap Job data
     * @throws SchedulerException If adding or updating the task fails
     */
    public void addOrUpdateCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron, JobDataMap jobDataMap)
            throws SchedulerException {
        LogUtils.info("AddOrUpdateCronJob: " + jobKey.getName() + "," + triggerKey.getGroup());

        if (scheduler.checkExists(triggerKey)) {
            modifyCronJobTime(triggerKey, cron);
        } else {
            addCronJob(jobKey, triggerKey, jobClass, cron, jobDataMap);
        }
    }

    /**
     * Adds or updates a Cron scheduled task (without JobDataMap).
     *
     * @param jobKey     Job identifier
     * @param triggerKey Trigger identifier
     * @param jobClass   Job class
     * @param cron       Cron expression
     * @throws SchedulerException If adding or updating the task fails
     */
    public void addOrUpdateCronJob(JobKey jobKey, TriggerKey triggerKey, Class jobClass, String cron) throws SchedulerException {
        addOrUpdateCronJob(jobKey, triggerKey, jobClass, cron, null);
    }

    /**
     * Gets the default JobDataMap, containing the basic information required for scheduled tasks.
     *
     * @param schedule Scheduled task object
     * @param expression Cron or time expression
     * @param userId    User ID executing the task
     * @return JobDataMap object
     */
    public JobDataMap getDefaultJobDataMap(Schedule schedule, String expression, String userId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("resourceId", schedule.getResourceId());
        jobDataMap.put("expression", expression);
        jobDataMap.put("userId", userId);
        jobDataMap.put("config", schedule.getConfig());
        jobDataMap.put("projectId", schedule.getProjectId());
        return jobDataMap;
    }
}