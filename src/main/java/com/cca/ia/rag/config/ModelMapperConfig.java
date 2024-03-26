package com.cca.ia.rag.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ccsw
 *
 */
@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper getModelMapper() {

        return new ModelMapper();
    }

}