package io.demo.common.schedule;

import io.demo.common.util.LogUtils;
import org.quartz.*;

/**
 * Base scheduling task class, all scheduling tasks should inherit from this class and implement specific business logic.
 * <p>
 * This class provides the resource information required for scheduling tasks and allows subclasses to implement specific business logic through the abstract method {@link #businessExecute(JobExecutionContext)}.
 * </p>
 *
 * @since 1.0
 */
public abstract class BaseScheduleJob implements Job {

    /**
     * Resource ID, indicating the resource associated with this task.
     */
    protected String resourceId;

    /**
     * User ID, indicating the user executing this task.
     */
    protected String userId;

    /**
     * Scheduling expression, used for task scheduling rules.
     */
    protected String expression;

    /**
     * Called when executing the scheduling task, extracts the information required for the task and calls the business execution method of the subclass.
     *
     * @param context The context object of the task execution
     * @throws JobExecutionException If the task execution fails, this exception is thrown
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Get the resource information required for the task from JobDataMap
        JobKey jobKey = context.getTrigger().getJobKey();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        this.resourceId = jobDataMap.getString("resourceId");
        this.userId = jobDataMap.getString("userId");
        this.expression = jobDataMap.getString("expression");

        // Log the current task execution status
        LogUtils.info(jobKey.getGroup() + " Running: " + resourceId);

        // Call the business logic implemented by the subclass
        businessExecute(context);
    }

    /**
     * Subclasses need to implement this method to define the specific business logic of task execution.
     *
     * @param context The context object of the task execution
     */
    protected abstract void businessExecute(JobExecutionContext context);
}