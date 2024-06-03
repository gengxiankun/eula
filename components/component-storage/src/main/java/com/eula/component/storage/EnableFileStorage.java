package com.eula.component.storage;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启动文件存储，自动根据配置文件加载
 * @author xiankun.geng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({FileStorageAutoConfiguration.class, FileStorageProperties.class})
public @interface EnableFileStorage {
}
