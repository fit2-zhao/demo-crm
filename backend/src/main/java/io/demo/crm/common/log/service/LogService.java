package io.demo.crm.common.log.service;

import io.demo.crm.services.system.domain.OperationLogBlob;
import io.demo.crm.common.log.dto.LogDTO;
import io.demo.crm.services.system.mapper.OperationLogBlobMapper;
import io.demo.crm.services.system.mapper.OperationLogMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 操作日志服务类
 * 提供单条和批量操作日志的存储方法。
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Resource
    private OperationLogBlobMapper operationLogBlobMapper;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 根据 LogDTO 创建一个 OperationLogBlob 实体对象。
     *
     * @param log 日志数据传输对象
     * @return OperationLogBlob
     */
    private OperationLogBlob getBlob(LogDTO log) {
        OperationLogBlob blob = new OperationLogBlob();
        blob.setId(log.getId());
        blob.setOriginalValue(log.getOriginalValue());
        blob.setModifiedValue(log.getModifiedValue());
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
    public void add(LogDTO log) {
        // 如果项目ID为空，设置为“none”
        if (StringUtils.isBlank(log.getProjectId())) {
            log.setProjectId("none");
        }
        // 如果创建用户为空，设置为“admin”
        if (StringUtils.isBlank(log.getCreateUser())) {
            log.setCreateUser("admin");
        }
        // 截断日志内容
        log.setContent(subStrContent(log.getContent()));

        // 插入操作日志和日志Blob数据
        operationLogMapper.insert(log);
        operationLogBlobMapper.insert(getBlob(log));
    }

    /**
     * 批量添加操作日志
     *
     * @param logs 日志数据传输对象列表
     */
    @Async
    public void batchAdd(List<LogDTO> logs) {
        // 如果日志列表为空，返回
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }

        // 开启批量操作的 SqlSession
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            OperationLogBlobMapper logBlobMapper = sqlSession.getMapper(OperationLogBlobMapper.class);

            // 批量插入日志数据
            if (CollectionUtils.isNotEmpty(logs)) {
                long currentTimeMillis = System.currentTimeMillis();
                logs.forEach(item -> {
                    item.setContent(subStrContent(item.getContent()));  // 截断内容
                    item.setCreateTime(currentTimeMillis);  // 设置创建时间

                    // 插入操作日志和日志Blob数据
                    operationLogMapper.insert(item);
                    logBlobMapper.insert(getBlob(item));
                });
            }

            // 提交批量操作
            sqlSession.flushStatements();
        }
    }
}
