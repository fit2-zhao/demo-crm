package io.demo.modules.system.service;

import io.demo.aspectj.dto.LogDTO;
import io.demo.aspectj.handler.OperationLogHandler;
import io.demo.common.uid.IDGenerator;
import io.demo.common.util.BeanUtils;
import io.demo.common.util.JSON;
import io.demo.modules.system.domain.OperationLog;
import io.demo.modules.system.domain.OperationLogBlob;
import io.demo.mybatis.BaseMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志服务类
 * 提供单条和批量操作日志的存储方法。
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LogService implements OperationLogHandler {

    @Resource
    private BaseMapper<OperationLog> operationLogMapper;

    @Resource
    private BaseMapper<OperationLogBlob> operationLogBlobMapper;

    /**
     * 根据 LogDTO 创建一个 OperationLogBlob 实体对象。
     *
     * @param log 日志数据传输对象
     * @return OperationLogBlob
     */
    private OperationLogBlob getBlob(LogDTO log) {
        OperationLogBlob blob = new OperationLogBlob();
        blob.setId(log.getId());

        if (log.getExtra() != null && ObjectUtils.isNotEmpty(log.getExtra().getOriginalValue())) {
            blob.setOriginalValue(JSON.toJSONBytes(log.getExtra().getOriginalValue()));
        }
        if (log.getExtra() != null && ObjectUtils.isNotEmpty(log.getExtra().getModifiedValue())) {
            blob.setModifiedValue(JSON.toJSONBytes(log.getExtra().getModifiedValue()));
        }
        return blob;
    }

    /**
     * 截断日志内容，确保其不超过500个字符。
     *
     * @param content 日志内容
     * @return 截取后的日志内容
     */
    private String subStrContent(String content) {
        if (StringUtils.isNotBlank(content) && content.length() > 500) {
            return content.substring(0, 499);
        }
        return content;
    }

    /**
     * 添加单条操作日志
     *
     * @param log 日志数据传输对象
     */
    @Async
    public void add(LogDTO log) {
        // 如果项目ID为空，设置为“none”
        if (StringUtils.isBlank(log.getProjectId())) {
            log.setProjectId("none");
        }
        if (StringUtils.isBlank(log.getOrganizationId())) {
            log.setOrganizationId("none");
        }
        // 如果创建用户为空，设置为“admin”
        if (StringUtils.isBlank(log.getCreateUser())) {
            log.setCreateUser("admin");
        }
        // 截断日志内容
        log.setContent(subStrContent(log.getContent()));

        // 插入操作日志和日志Blob数据
        log.setId(IDGenerator.nextStr());
        operationLogMapper.insert(BeanUtils.copyBean(new OperationLog(), log));
        operationLogBlobMapper.insert(getBlob(log));
    }

    /**
     * 批量添加操作日志
     *
     * @param logs 日志数据传输对象列表
     */
    @Async
    public void batchAdd(List<LogDTO> logs) {
        // 如果日志列表为空，直接返回
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }

        var currentTimeMillis = System.currentTimeMillis();
        List<OperationLog> items = new ArrayList<>();
        // 使用流处理，构建操作日志和Blob列表
        var blobs = logs.stream()
                .peek(log -> {
                    log.setId(IDGenerator.nextStr());
                    log.setContent(subStrContent(log.getContent()));
                    log.setCreateTime(currentTimeMillis);
                    OperationLog item = new OperationLog();
                    BeanUtils.copyBean(item, log);
                    items.add(item);
                })
                .map(this::getBlob)
                .toList();
        // 批量插入操作日志和日志Blob数据
        operationLogMapper.batchInsert(items);
        operationLogBlobMapper.batchInsert(blobs);
    }

    @Override
    public void handleLog(LogDTO operationLog) {
        add(operationLog);
    }
}
