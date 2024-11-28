package io.demo.aspectj.handler;

import io.demo.aspectj.dto.LogDTO;

import java.util.List;

public interface OperationLogHandler {
    /**
     * 处理日志
     *
     * @param operationLogs 操作日志
     */
    void handleLog(List<LogDTO> operationLogs);

}
