package com.eula.component.storage;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiankun.geng
 */
@Data
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件 ID
     */
    private Long id;

    /**
     * 文件访问地址
     */
    private String url;

    /**
     * 文件大小，单位字节
     */
    private Long size;

    /**
     * 文件名称
     */
    private String filename;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 基础存储路径
     */
    private String basePath;

    /**
     * 存储路径
     */
    private String path;

    /**
     * 文件扩展名
     */
    private String ext;

    /**
     * MIME 类型
     */
    private String contentType;

    /**
     * 存储平台
     */
    private String platform;

    /**
     * 文件所属对象id
     */
    private String objectId;

    /**
     * 文件所属对象类型，例如用户头像，评价图片
     */
    private String objectType;

    /**
     * 获取对象存储中文件的唯一标识
     * @return 文件唯一标识
     */
    public String getFileKey() {
        return this.getBasePath() + this.getPath() + this.getFilename();
    }

}
