package io.demo.aspectj.handler;

import io.demo.aspectj.dto.LogDTO;

public interface OperationLogHandler {
    /**
     * 处理日志
     *
     * @param operationLog 操作日志
     */
    void handleLog(LogDTO operationLog);

}
