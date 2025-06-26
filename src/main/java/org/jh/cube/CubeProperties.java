package org.jh.cube;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author SugarMGP
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cube")
public class CubeProperties {
    private String baseUrl;
    private String apiKey;
    private String bucketName;
}
