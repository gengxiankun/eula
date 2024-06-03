package com.eula.component.storage.platform;

import cn.hutool.core.io.FileUtil;
import com.eula.component.storage.FileInfo;
import com.eula.component.storage.UploadPretreatment;
import com.eula.component.storage.exception.FileStorageRuntimeException;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

@Getter
@Setter
public class LocalFileStorage implements FileStorage {

    /**
     * 本地存储路径
     */
    private String basePath;

    /**
     * 存储平台
     */
    private String platform;

    /**
     * 访问域名
     */
    private String domain;

    @Override
    public FileInfo upload(FileInfo fileInfo, UploadPretreatment pre) {
        String path = fileInfo.getPath();

        File newFile = FileUtil.touch(this.basePath + path, fileInfo.getFilename());
        fileInfo.setBasePath(this.basePath);
        fileInfo.setUrl(this.domain + path + fileInfo.getFilename());

        try {
            pre.getFileWrapper().transferTo(newFile);
            return fileInfo;
        } catch (IOException e) {
            FileUtil.del(newFile);
            throw new FileStorageRuntimeException("文件上传失败！");
        }
    }

    @Override
    public void delete(FileInfo fileInfo) {
        FileUtil.del(new File(fileInfo.getBasePath() + fileInfo.getPath(),fileInfo.getFilename()));
    }

    @Override
    public boolean exists(FileInfo fileInfo) {
        return new File(fileInfo.getBasePath() + fileInfo.getPath(),fileInfo.getFilename()).exists();
    }

    @Override
    public void download(FileInfo fileInfo, Consumer<InputStream> consumer) {
        try (InputStream in = FileUtil.getInputStream(fileInfo.getBasePath() + fileInfo.getPath() + fileInfo.getFilename())) {
            consumer.accept(in);
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件下载失败！platform：" + fileInfo,e);
        }
    }

    @Override
    public void close() {

    }

}
