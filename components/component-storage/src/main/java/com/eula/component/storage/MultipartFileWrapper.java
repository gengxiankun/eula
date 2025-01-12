package com.eula.component.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * MultipartFile 包装类
 * @author xiankun.geng
 */
public class MultipartFileWrapper implements MultipartFile {

    @Setter
    private String name;

    @Setter
    private String originalFilename;

    @Setter
    private String contentType;

    @Setter
    @Getter
    private MultipartFile multipartFile;

    public MultipartFileWrapper(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }

    @Override
    public String getName() {
        return name != null ? name : multipartFile.getName();
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename != null ? originalFilename : multipartFile.getOriginalFilename();
    }

    @Override
    public String getContentType() {
        return contentType != null ? contentType : multipartFile.getContentType();
    }

    @Override
    public boolean isEmpty() {
        return multipartFile.isEmpty();
    }

    @Override
    public long getSize() {
        return multipartFile.getSize();
    }

    @Override
    public byte[] getBytes() throws IOException {
        return multipartFile.getBytes();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return multipartFile.getInputStream();
    }

    @Override
    public Resource getResource() {
        return multipartFile.getResource();
    }

    @Override
    public void transferTo(Path dest) throws IOException, IllegalStateException {
        multipartFile.transferTo(dest);
    }

    @Override
    public void transferTo(File file) throws IOException, IllegalStateException {
        multipartFile.transferTo(file);
    }

}
