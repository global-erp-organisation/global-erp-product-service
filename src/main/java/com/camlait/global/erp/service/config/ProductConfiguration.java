package com.camlait.global.erp.service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.camlait.global.erp.domain.config.GlobalAppConstants;

@Configuration
@EntityScan(basePackages = {GlobalAppConstants.DOMAIN_BASE_PACKAGE})
@EnableJpaRepositories(basePackages = {GlobalAppConstants.DAO_BASE_PACKAGE})
@ComponentScan(basePackages = {GlobalAppConstants.DELEGATE_BASE_PACKAGE, GlobalAppConstants.SERVICE_BASE_PACKAGE})
public class ProductConfiguration {

}
