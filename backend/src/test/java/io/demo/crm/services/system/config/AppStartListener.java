package io.demo.crm.services.system.config;

import io.demo.crm.common.uid.impl.DefaultUidGenerator;
import io.demo.crm.common.util.LogUtils;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AppStartListener implements ApplicationRunner {

    @Resource
    private DefaultUidGenerator defaultUidGenerator;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LogUtils.info("================= 应用启动 =================");
        defaultUidGenerator.init();
    }
}
