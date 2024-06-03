package com.eula.component.storage;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 文件上传预处理对象
 * @author xiankun.geng
 */
@Data
@Accessors(chain = true)
public class UploadPretreatment {

    private String platform;

    private FileStorageTemplate fileStorageTemplate;

    /**
     * 要上传的文件包装类
     */
    private MultipartFileWrapper fileWrapper;

    /**
     * 文件存储路径
     */
    private String path = "";

    /**
     * 保存文件名，如果不设置则自动生成
     */
    private String saveFilename;

    /**
     * MIME 类型，如果不设置则在上传文件根据 {@link MultipartFileWrapper#getContentType()} 和文件名自动识别
     */
    private String contentType;

    /**
     * 获取文件名
     */
    public String getName() {
        return fileWrapper.getName();
    }

    /**
     * 获取原始文件名
     */
    public String getOriginalFilename() {
        return fileWrapper.getOriginalFilename();
    }

    public FileInfo upload() {
        return fileStorageTemplate.upload(this);
    }

}
