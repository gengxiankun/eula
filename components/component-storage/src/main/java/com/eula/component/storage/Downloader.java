package com.eula.component.storage;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.eula.component.storage.platform.FileStorage;

import java.io.File;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * @author xiankun.geng
 */
public class Downloader {

    private final FileStorage fileStorage;

    private final FileInfo fileInfo;

    /**
     * 构造下载器
     */
    public Downloader(FileInfo fileInfo, FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.fileInfo = fileInfo;
    }

    public void inputStream(Consumer<InputStream> consumer) {
        this.fileStorage.download(this.fileInfo, consumer);
    }

    /**
     * 下载 byte 数组
     */
    public byte[] bytes() {
        byte[][] bytes = new byte[1][];
        inputStream(in -> bytes[0] = IoUtil.readBytes(in));
        return bytes[0];
    }

    /**
     * 下载到指定文件
     */
    public void file(File file) {
        inputStream(in -> FileUtil.writeFromStream(in, file));
    }

    /**
     * 下载到指定文件
     */
    public void file(String filename) {
        inputStream(in -> FileUtil.writeFromStream(in, filename));
    }

}
