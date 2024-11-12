package io.demo.crm;

import io.demo.crm.config.MinioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(exclude = {
        QuartzAutoConfiguration.class,  // 禁用 Quartz 自动配置
        LdapAutoConfiguration.class,    // 禁用 LDAP 自动配置
        Neo4jAutoConfiguration.class    // 禁用 Neo4j 自动配置
})
@PropertySource(value = {
        "classpath:commons.properties",   // 加载类路径下的配置文件
        "file:/opt/demo/conf/demo.properties",  // 加载外部配置文件
}, encoding = "UTF-8", ignoreResourceNotFound = true)  // 忽略配置文件未找到的错误
@ServletComponentScan // 启用 Servlet 组件扫描，支持过滤器和监听器
@EnableConfigurationProperties({
        MinioProperties.class,  // 启用自定义配置类的绑定
})
public class Application {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(Application.class, args);
    }
}
