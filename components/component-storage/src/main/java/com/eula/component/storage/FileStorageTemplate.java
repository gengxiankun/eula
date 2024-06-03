package com.eula.component.storage;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.eula.component.storage.exception.FileStorageRuntimeException;
import com.eula.component.storage.platform.FileStorage;
import com.eula.component.storage.record.FileRecorder;
import com.eula.component.storage.tika.TikaFactory;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author xiankun.geng
 */
@Data
public class FileStorageTemplate implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageTemplate.class);

    private FileStorageTemplate self;

    private FileStorageProperties properties;

    private CopyOnWriteArrayList<FileStorage> fileStorageList;

    private FileRecorder fileRecorder;

    private TikaFactory tikaFactory;

    public FileStorage getFileStorage(String platform) {
        for (FileStorage fileStorage : fileStorageList) {
            if (fileStorage.getPlatform().equals(platform)) {
                return fileStorage;
            }
        }
        return null;
    }

    /**
     * 上传文件
     */
    public FileInfo upload(UploadPretreatment pre) {
        MultipartFile file = pre.getFileWrapper();
        if (file == null) {
            throw new FileStorageRuntimeException("文件不允许为空！");
        }
        FileInfo fileInfo = this.buildFileInfo(pre);

        FileStorage fileStorage = this.getFileStorage(pre.getPlatform());
        if (fileStorage == null) {
            throw new FileStorageRuntimeException("没有找到对应存储平台！");
        }
        fileInfo = fileStorage.upload(fileInfo, pre);
        this.getFileRecorder().record(fileInfo);

        return fileInfo;
    }

    /**
     * 根据 url 删除文件
     */
    public boolean delete(String url) {
        return delete(this.getFileInfoByUrl(url));
    }

    /**
     * 根据 FileInfo 删除文件
     */
    public boolean delete(FileInfo fileInfo) {
        if (fileInfo == null) {
            return false;
        }
        FileStorage fileStorage = this.getFileStorage(fileInfo.getPlatform());
        if (fileStorage == null) {
            throw new FileStorageRuntimeException("没有找到对应存储平台！");
        }
        fileStorage.delete(fileInfo);
        return this.getFileRecorder().delete(fileInfo.getUrl());
    }

    /**
     * 根据 url 判断文件是否存在
     */
    public boolean exists(String url) {
        return exists(this.getFileInfoByUrl(url));
    }

    /**
     * 根据 fileInfo 判断文件是否存在
     */
    public boolean exists(FileInfo fileInfo) {
        if (fileInfo == null) {
            return false;
        }
        FileStorage fileStorage = this.getFileStorage(fileInfo.getPlatform());
        if (fileStorage == null) {
            throw new FileStorageRuntimeException("没有找到对应存储平台！");
        }
        return fileStorage.exists(fileInfo);
    }

    public Downloader download(String url) {
        return this.download(this.getFileInfoByUrl(url));
    }

    public Downloader download(FileInfo fileInfo) {
        if (fileInfo == null) {
            return null;
        }
        FileStorage fileStorage = this.getFileStorage(fileInfo.getPlatform());
        if (fileStorage == null) {
            throw new FileStorageRuntimeException("没有找到对应存储平台！");
        }
        return new Downloader(fileInfo, fileStorage);
    }

    /**
     * 根据 url 获取 FileInfo
     */
    private FileInfo getFileInfoByUrl(String url) {
        return fileRecorder.getByUrl(url);
    }

    /**
     * 根据上传文件预处理对象构建 FileInfo
     */
    private FileInfo buildFileInfo(UploadPretreatment pre) {
        MultipartFile file = pre.getFileWrapper();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setPlatform(pre.getPlatform());
        fileInfo.setSize(file.getSize());
        fileInfo.setOriginalFilename(file.getOriginalFilename());
        fileInfo.setExt(FileNameUtil.getSuffix(file.getOriginalFilename()));
        fileInfo.setPath(pre.getPath());
        if (CharSequenceUtil.isNotBlank(pre.getSaveFilename())) {
            fileInfo.setFilename(pre.getSaveFilename());
        } else {
            fileInfo.setFilename(IdUtil.objectId() + (CharSequenceUtil.isEmpty(fileInfo.getExt()) ? CharSequenceUtil.EMPTY : "." + fileInfo.getExt()));
        }
        if (pre.getContentType() != null) {
            fileInfo.setContentType(pre.getContentType());
        } else {
            fileInfo.setContentType(file.getContentType());
        }
        return fileInfo;
    }

    /**
     * 创建上传预处理器
     */
    public UploadPretreatment of() {
        UploadPretreatment pre = new UploadPretreatment();
        pre.setFileStorageTemplate(self);
        pre.setPlatform(properties.getDefaultPlatform());
        return pre;
    }

    /**
     * 根据 MultipartFile 创建上传预处理器
     */
    public UploadPretreatment of(MultipartFile multipartFile) {
        UploadPretreatment pre = of();
        pre.setFileWrapper(new MultipartFileWrapper(multipartFile));
        return pre;
    }

    /**
     * 根据 byte 数据创建上传预处理器
     */
    public UploadPretreatment of(byte[] bytes) {
        UploadPretreatment pre = of();
        String contentType = this.tikaFactory.getTika().detect(bytes);
        pre.setFileWrapper(new MultipartFileWrapper(new MockMultipartFile("", "", contentType, bytes)));
        return pre;
    }

    /**
     * 根据 InputStream 创建上传预处理器
     */
    public UploadPretreatment of(InputStream in) {
        try {
            UploadPretreatment pre = of();
            byte[] bytes = IoUtil.readBytes(in);
            String contentType = this.tikaFactory.getTika().detect(bytes);
            pre.setFileWrapper(new MultipartFileWrapper(new MockMultipartFile("", "", contentType, bytes)));
            return pre;
        } catch (Exception e) {
            throw new FileStorageRuntimeException("根据 InputStream 创建上传预处理器失败！", e);
        }
    }

    /**
     * 根据 File 创建上传预处理器
     */
    public UploadPretreatment of(File file) {
        try {
            UploadPretreatment pre = of();
            String contentType = this.tikaFactory.getTika().detect(file);
            pre.setFileWrapper(new MultipartFileWrapper(new MockMultipartFile(file.getName(), file.getName(), contentType, Files.newInputStream(file.toPath()))));
            return pre;
        } catch (Exception e) {
            throw new FileStorageRuntimeException("根据 File 创建上传预处理器失败！", e);
        }
    }

    /**
     * 根据 URL 创建上传预处理器，originalFilename 将尝试自动识别，识别不到则为空字符串
     */
    public UploadPretreatment of(URL url) {
        try {
            UploadPretreatment pre = of();

            URLConnection conn = url.openConnection();

            // 尝试获取文件名
            String name = "";
            String disposition = conn.getHeaderField("Content-Disposition");
            if (StrUtil.isNotBlank(disposition)) {
                name = ReUtil.get("filename=\"(.*?)\"",disposition,1);
                if (StrUtil.isBlank(name)) {
                    name = StrUtil.subAfter(disposition,"filename=",true);
                }
            }
            if (StrUtil.isBlank(name)) {
                final String path = url.getPath();
                name = StrUtil.subSuf(path,path.lastIndexOf('/') + 1);
                if (StrUtil.isNotBlank(name)) {
                    name = URLUtil.decode(name, StandardCharsets.UTF_8);
                }
            }

            byte[] bytes = IoUtil.readBytes(conn.getInputStream());
            String contentType = tikaFactory.getTika().detect(bytes, name);
            pre.setFileWrapper(new MultipartFileWrapper(new MockMultipartFile(url.toString(),name,contentType,bytes)));
            return pre;
        } catch (Exception e) {
            throw new FileStorageRuntimeException("根据 URL 创建上传预处理器失败！",e);
        }
    }

    /**
     * 销毁容器时，将存储平台进行销毁
     */
    @Override
    public void destroy() {
        for (FileStorage fileStorage : fileStorageList) {
            try {
                fileStorage.close();
                logger.info("销毁存储平台 {} 成功", fileStorage.getPlatform());
            } catch (Exception e) {
                logger.error("销毁存储平台 {} 失败，{}", fileStorage.getPlatform(), e.getMessage(), e);
            }
        }
    }

}
