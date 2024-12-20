
package io.demo.modules.system.service;

import io.demo.common.uid.worker.WorkerIdAssigner;
import io.demo.common.uid.worker.WorkerNodeType;
import io.demo.common.util.LogUtils;
import io.demo.modules.system.domain.WorkerNode;
import io.demo.modules.system.mapper.ExtWorkerNodeMapper;
import io.demo.common.uid.utils.DockerUtils;
import io.demo.common.uid.utils.NetUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

/**
 * Represents an implementation of {@link WorkerIdAssigner},
 * the worker id will be discarded after assigned to the UidGenerator
 */
@Service
public class DisposableWorkerIdAssigner implements WorkerIdAssigner {
    @Resource
    private ExtWorkerNodeMapper extWorkerNodeMapper;

    /**
     * Assign worker id base on database.<p>
     * If there is host name & port in the environment, we considered that the node runs in Docker container<br>
     * Otherwise, the node runs on an actual machine.
     *
     * @return assigned worker id
     */
    public long assignWorkerId() {
        // build worker node entity
        try {
            WorkerNode workerNode = buildWorkerNode();

            // add worker node for new (ignore the same IP + PORT)
            extWorkerNodeMapper.insert(workerNode);
            LogUtils.info("Add worker node:" + workerNode);

            return workerNode.getId();
        } catch (Exception e) {
            LogUtils.error("Assign worker id exception. ", e);
            return 1;
        }
    }

    /**
     * Build worker node entity by IP and PORT
     */
    private WorkerNode buildWorkerNode() {
        WorkerNode workerNode = new WorkerNode();
        if (DockerUtils.isDocker()) {
            workerNode.setType(WorkerNodeType.CONTAINER.value());
            workerNode.setHostName(DockerUtils.getDockerHost());
            workerNode.setPort(DockerUtils.getDockerPort());

        } else {
            workerNode.setType(WorkerNodeType.ACTUAL.value());
            workerNode.setHostName(NetUtils.getLocalAddress());
            workerNode.setPort(System.currentTimeMillis() + "-" + RandomUtils.nextInt());
        }
        workerNode.setCreated(System.currentTimeMillis());
        workerNode.setModified(System.currentTimeMillis());
        workerNode.setLaunchDate(System.currentTimeMillis());
        return workerNode;
    }

}
