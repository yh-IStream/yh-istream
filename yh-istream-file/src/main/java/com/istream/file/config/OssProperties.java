package com.istream.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    private String type = "local";

    private Local local = new Local();

    @Data
    public static class Local {
        private String uploadPath = "./uploads";
        private String urlPrefix = "/files";
    }
}