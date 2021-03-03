package com.atguigu.gmall.sms.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置
 */
@Configuration
public class DataSourceConfig {

    /**
     * @return The default datasource
     * @ConfigurationProperties(prefix = "spring.datasource")是调用返回对象的set方法
     * 故需要使用@Value()来注入数据源的属性值
     * <p>
     * 需要将 DataSourceProxy 设置为主数据源，否则事务无法回滚
     */
    @Primary
    @Bean("dataSource")
    public DataSource dataSource(
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.driver-class-name}") String driverClassName
    ) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setUsername(username);
        hikariDataSource.setPassword(password);
        hikariDataSource.setDriverClassName(driverClassName);
        return new DataSourceProxy(hikariDataSource);
    }
}
