package io.demo.aspectj.handler;

import io.demo.aspectj.dto.LogExtraDTO;
import io.demo.common.util.ServletUtils;
import io.demo.aspectj.dto.LogDTO;
import io.demo.aspectj.builder.LogRecord;
import io.demo.common.util.JSON;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * 操作日志 ILogRecordService 实现类
 * <p>
 * 基于 {@link OperationLogHandler} 实现，记录操作日志
 * 使用参考 {@link <a href="https://github.com/mouzt/mzt-biz-log">...</a>}
 */
@Service
public class LogRecordService {

    @Resource
    private OperationLogHandler operationLogHandler;

    public void record(LogRecord logRecord) {
        // 1. 补全通用字段
        LogDTO reqDTO = new LogDTO();
        // 补全模块信息
        fillModuleFields(reqDTO, logRecord);
        // 补全请求信息
        fillRequestFields(reqDTO);

        // todo： 组织或项目信息

        // 2. 异步记录日志
        assert operationLogHandler != null;
        operationLogHandler.handleLog(reqDTO);
    }


    public static void fillModuleFields(LogDTO reqDTO, LogRecord logRecord) {
        reqDTO.setCreateTime(System.currentTimeMillis());
        reqDTO.setType(logRecord.getType()); // 大模块类型，例如：CRM 客户
        reqDTO.setCreateUser(logRecord.getOperator());
        reqDTO.setModule(logRecord.getSubType());// 操作类型：CURD
        reqDTO.setSourceId(logRecord.getBizNo()); // 资源id
        reqDTO.setContent(logRecord.getAction());// 操作内容，例如：修改编号为 1 的用户信息，将性别从男改成女

        // 变更原始内容
        if (logRecord.getExtra() != null) {
            reqDTO.setExtra(JSON.parseObject(logRecord.getExtra(), LogExtraDTO.class));
        }
    }

    private static void fillRequestFields(LogDTO reqDTO) {
        // 获得 Request 对象
        HttpServletRequest request = ServletUtils.getRequest();
        if (request == null) {
            return;
        }
        // 补全请求信息
        reqDTO.setMethod(request.getMethod());
        reqDTO.setPath(request.getRequestURI());
    }
}