package com.eula.component.storage.platform;

import com.eula.component.storage.FileInfo;
import com.eula.component.storage.UploadPretreatment;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * @author xiankun.geng
 */
public interface FileStorage {

    /**
     * 获取平台
     * @return 平台
     */
    String getPlatform();

    /**
     * 设置平台
     * @param platform 平台
     */
    void setPlatform(String platform);

    /**
     * 上传文件
     * @param fileInfo 文件详情
     * @param pre 文件上传预处理对象
     * @return 文件详情
     */
    FileInfo upload(FileInfo fileInfo, UploadPretreatment pre);

    /**
     * 删除文件
     * @param fileInfo 文件详情
     */
    void delete(FileInfo fileInfo);

    /**
     * 文件是否存在
     * @param fileInfo 文件详情
     * @return 是否存在
     */
    boolean exists(FileInfo fileInfo);

    void download(FileInfo fileInfo, Consumer<InputStream> consumer);

    /**
     * 关闭平台
     */
    void close();

}
