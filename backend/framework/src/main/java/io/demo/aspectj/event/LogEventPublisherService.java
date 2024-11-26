package io.demo.aspectj.event;

import io.demo.aspectj.dto.LogDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 日志事件发布服务。
 * <p>
 * 该服务负责将日志事件发布到 Spring 应用上下文，
 * 以实现基于事件驱动的日志处理任务。
 */
@Service
public class LogEventPublisherService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造方法，注入事件发布器。
     *
     * @param eventPublisher 用于发布事件的 ApplicationEventPublisher
     */
    @Autowired
    public LogEventPublisherService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 发布日志事件。
     * <p>
     * 接收日志数据并封装为 LogEvent 事件发布。参数列表会被转换为不可变集合，
     * 以确保事件数据的完整性。
     *
     * @param logs 日志数据列表，包含若干 LogDTO 对象
     * @throws IllegalArgumentException 如果日志列表为 null 或为空
     */
    public void publishLogEvent(List<LogDTO> logs) {
        if (logs == null || logs.isEmpty()) {
            throw new IllegalArgumentException("日志列表不能为空！");
        }
        // 转换为不可变集合，确保数据安全性
        List<LogDTO> immutableLogs = List.copyOf(logs);
        // 创建并发布日志事件
        LogEvent event = new LogEvent(immutableLogs);
        eventPublisher.publishEvent(event);
    }
}
