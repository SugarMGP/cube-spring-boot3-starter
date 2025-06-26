package org.jh.cube;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author SugarMGP
 */
@Configuration
@ConditionalOnClass(CubeService.class)
@EnableConfigurationProperties(CubeProperties.class)
public class CubeAutoConfiguration {
    @Bean
    public CubeService cubeService(CubeProperties properties) {
        return new CubeService(properties);
    }
}