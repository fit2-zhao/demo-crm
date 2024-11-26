package io.demo.aspectj.event;

import io.demo.aspectj.dto.LogDTO;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogEventPublisherService {

    @Resource
    private ApplicationEventPublisher eventPublisher;

    public void publishEvent(List<LogDTO> logs) {
        LogEvent event = new LogEvent(logs);
        eventPublisher.publishEvent(event);
    }
}
