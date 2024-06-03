package com.eula.component.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiankun.geng
 */
@Data
@Component
@ConfigurationProperties(prefix = "component.file-storage")
public class FileStorageProperties {

    /**
     * 默认存储平台
     */
    private String defaultPlatform = "tencent-cos";

    /**
     * 本地存储
     */
    private List<Local> local = new ArrayList<>();

    /**
     * 腾讯云 COS
     */
    private List<TencentCos> tencentCos = new ArrayList<>();

    /**
     * 本地存储
     */
    @Data
    public static class Local {

        /**
         * 本地存储路径
         */
        private String basePath = "";

        /**
         * 本地存储访问路径
         */
        private String[] pathPatterns = new String[0];

        /**
         * 启用本地存储
         */
        private Boolean enableStorage = false;

        /**
         * 启用本地访问
         */
        private Boolean enableAccess = false;

        /**
         * 存储平台
         */
        private String platform = "local";

        /**
         * 访问域名
         */
        private String domain = "";

    }

    /**
     * 腾讯云 COS
     */
    @Data
    public static class TencentCos {

        private String platform = "";

        private Boolean enable = false;

        private String secretId;

        private String secretKey;

        private String region;

        private String bucketName;

        private String domain = "";

        private String basePath = "";

    }

}
