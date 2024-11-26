package io.demo.aspectj.event;

import io.demo.aspectj.dto.LogDTO;
import lombok.Getter;

import java.util.List;

/**
 * 日志事件
 */
public class LogEvent {
    @Getter
    private final List<LogDTO> logs;

    public LogEvent(List<LogDTO> logs) {
        this.logs = logs;
    }

}
