package io.demo.common.util;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

public class HikariCPUtils {
    /**
     * Get the usage status of the HikariCP connection pool.
     *
     * @param dataSource HikariDataSource instance
     * @return HikariCP connection pool status information
     */
    public static String getHikariCPStatus(HikariDataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("HikariDataSource cannot be null");
        }

        HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();

        return "HikariCP Status:\n" +
                "Active Connections: " + poolMXBean.getActiveConnections() + "\n" +
                "Idle Connections: " + poolMXBean.getIdleConnections() + "\n" +
                "Total Connections: " + poolMXBean.getTotalConnections() + "\n" +
                "Threads Awaiting Connection: " + poolMXBean.getThreadsAwaitingConnection() + "\n";
    }

    /**
     * Get the configuration of the HikariCP connection pool.
     *
     * @param dataSource HikariDataSource instance
     * @return Connection pool configuration
     */
    public static String getHikariCPConfig(HikariDataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("HikariDataSource cannot be null");
        }

        return "HikariCP Configuration:\n" +
                "Maximum Pool Size: " + dataSource.getMaximumPoolSize() + "\n" +
                "Minimum Idle Connections: " + dataSource.getMinimumIdle() + "\n" +
                "Connection Timeout: " + dataSource.getConnectionTimeout() + " ms\n" +
                "Idle Timeout: " + dataSource.getIdleTimeout() + " ms\n" +
                "Max Lifetime: " + dataSource.getMaxLifetime() + " ms\n";
    }

    /**
     * Print the status and configuration information of HikariCP.
     */
    public static void printHikariCPStatus() {
        HikariDataSource dataSource = CommonBeanFactory.getBean(HikariDataSource.class);
        if (dataSource == null) {
            LogUtils.error("HikariDataSource not found");
            return;
        }
        LogUtils.info(getHikariCPStatus(dataSource));
        LogUtils.info(getHikariCPConfig(dataSource));
    }
}