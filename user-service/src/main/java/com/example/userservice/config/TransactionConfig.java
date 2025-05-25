package com.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {

   @Bean(name = "transactionManager")
   @Primary
   public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
       return new JpaTransactionManager(entityManagerFactory);
   }
}