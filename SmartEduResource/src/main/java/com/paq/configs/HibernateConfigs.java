/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paq.configs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import javax.sql.DataSource;
import static org.hibernate.cfg.JdbcSettings.DIALECT;
import static org.hibernate.cfg.JdbcSettings.SHOW_SQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

/**
 *
 * @author admin
 */
@Configuration
@PropertySource(value = "classpath:databases.properties", ignoreResourceNotFound = true)
public class HibernateConfigs {

    @Autowired
    private Environment env;

    @Bean
    public LocalSessionFactoryBean getSessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setPackagesToScan(new String[]{"com.paq.pojo"});
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("hibernate.connection.driverClass", "com.mysql.cj.jdbc.Driver"));
        String instanceConnectionName = env.getProperty("INSTANCE_CONNECTION_NAME");

        if (instanceConnectionName != null && !instanceConnectionName.isBlank()) {
            Properties props = new Properties();
            props.setProperty("socketFactory", "com.google.cloud.sql.mysql.SocketFactory");
            props.setProperty("cloudSqlInstance", instanceConnectionName);
            props.setProperty("cloudSqlRefreshStrategy", "lazy");

            dataSource.setUrl("jdbc:mysql:///" + env.getRequiredProperty("DB_NAME"));
            dataSource.setUsername(env.getRequiredProperty("DB_USER"));
            dataSource.setPassword(env.getRequiredProperty("DB_PASS"));
            dataSource.setConnectionProperties(props);
        } else {
            dataSource.setUrl(env.getProperty("hibernate.connection.url"));
            dataSource.setUsername(env.getProperty("hibernate.connection.username"));
            dataSource.setPassword(env.getProperty("hibernate.connection.password"));
        }

        HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        config.setMaximumPoolSize(env.getProperty("DB_MAX_POOL_SIZE", Integer.class, 5));
        config.setMinimumIdle(env.getProperty("DB_MIN_IDLE", Integer.class, 1));

        return new HikariDataSource(config);
    }

    private Properties hibernateProperties() {
        Properties props = new Properties();
        props.put(DIALECT, env.getProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"));
        props.put(SHOW_SQL, env.getProperty("hibernate.showSql", "false"));
        return props;
    }

    @Bean
    public HibernateTransactionManager transactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(getSessionFactory().getObject());
        return transactionManager;
    }
}
