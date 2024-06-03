package com.eula.component.storage;

import cn.hutool.core.collection.CollUtil;
import com.eula.component.storage.platform.FileStorage;
import com.eula.component.storage.platform.LocalFileStorage;
import com.eula.component.storage.platform.TencentCosFileFileStorage;
import com.eula.component.storage.record.DefaultFileRecorder;
import com.eula.component.storage.record.FileRecorder;
import com.eula.component.storage.tika.DefaultTikaFactory;
import com.eula.component.storage.tika.TikaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author xiankun.geng
 */
@Configuration
@ConditionalOnMissingBean(FileStorageTemplate.class)
public class FileStorageAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageAutoConfiguration.class);

    @Autowired
    private FileStorageProperties properties;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 本地存储 Bean
     */
    @Bean
    public List<LocalFileStorage> localFileStorageList() {
        return properties.getLocal().stream().map(local -> {
            if (!local.getEnableStorage()) return null;
            logger.info("加载存储平台：{}",local.getPlatform());
            LocalFileStorage localFileStorage = new LocalFileStorage();
            localFileStorage.setPlatform(local.getPlatform());
            localFileStorage.setBasePath(local.getBasePath());
            localFileStorage.setDomain(local.getDomain());
            return localFileStorage;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 腾讯云 COS 存储 Bean
     */
    @Bean
    @ConditionalOnClass(name = "com.qcloud.cos.COSClient")
    public List<TencentCosFileFileStorage> tencentCosFileStorageList() {
        return properties.getTencentCos().stream().map(cos -> {
           if (Boolean.FALSE.equals(cos.getEnable())) {
               return null;
           }
           TencentCosFileFileStorage storage = new TencentCosFileFileStorage();
           storage.setPlatform(cos.getPlatform());
           storage.setSecretId(cos.getSecretId());
           storage.setSecretKey(cos.getSecretKey());
           storage.setRegion(cos.getRegion());
           storage.setDomain(cos.getDomain());
           storage.setBasePath(cos.getBasePath());
           storage.setBucketName(cos.getBucketName());
           return storage;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Bean
    @ConditionalOnMissingBean(FileRecorder.class)
    public FileRecorder fileRecorder() {
        return new DefaultFileRecorder();
    }

    @Bean
    @ConditionalOnMissingBean(TikaFactory.class)
    public TikaFactory tikaFactory() {
        return new DefaultTikaFactory();
    }

    @Bean
    public FileStorageTemplate fileStorageTemplate(FileRecorder fileRecorder,
                                                   List<List<? extends FileStorage>> fileStorageLists,
                                                   TikaFactory tikaFactory) {
        this.initDetect();
        FileStorageTemplate service = new FileStorageTemplate();
        service.setFileRecorder(fileRecorder);
        service.setFileStorageList(new CopyOnWriteArrayList<>());
        fileStorageLists.forEach(service.getFileStorageList()::addAll);
        service.setProperties(properties);
        service.setTikaFactory(tikaFactory);
        return service;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent() {
        FileStorageTemplate service = applicationContext.getBean(FileStorageTemplate.class);
        service.setSelf(service);
    }

    public void initDetect() {
        String template = "检测到{}配置，但是没有找到对应的依赖库，所以无法加载此存储平台！";
        if (CollUtil.isNotEmpty(properties.getTencentCos()) && doesNotExistClass("com.qcloud.cos.COSClient")) {
            logger.warn(template, " 腾讯云 COS ");
        }
    }

    /**
     * 判断是否没有引入指定 Class
     */
    public static boolean doesNotExistClass(String name) {
        try {
            Class.forName(name);
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

}
