package com.example.back.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

// HokaroCP 이용해서 mySQL연결
@Configuration
@PropertySource("classpath:/application.yml")
public class DatabaseConfiguration {
   private static final Logger logger = LogManager.getLogger(DatabaseConfiguration.class);
   @Bean
   @ConfigurationProperties(prefix = "spring.datasource.hikari")
   public HikariConfig hikariConfig() {//HikariCP 환경 설정을 해줌. - 속성값이 필요해 -> application.yml에 있어.
      return new HikariConfig();
   }

   @Bean
   public DataSource dataSource() {
      DataSource dataSource = new HikariDataSource(hikariConfig());
      logger.info("datasource : {}", dataSource);
      return dataSource;
   }
   @Autowired
   private ApplicationContext applicationContext;
   @Bean
   public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
      SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
      sqlSessionFactoryBean.setDataSource(dataSource);
      sqlSessionFactoryBean.setMapperLocations(applicationContext.getResources("classpath:/mapper/**/*.xml"));
      return sqlSessionFactoryBean.getObject();
   }
   @Bean
   public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
      return new SqlSessionTemplate(sqlSessionFactory);
   }
}
