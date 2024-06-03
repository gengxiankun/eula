package com.eula.component.storage.platform;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;
import com.eula.component.storage.FileInfo;
import com.eula.component.storage.UploadPretreatment;
import com.eula.component.storage.exception.FileStorageRuntimeException;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * @author xiankun.geng
 */
@Data
public class TencentCosFileFileStorage implements FileStorage {

    private String platform;

    private String secretId;

    private String secretKey;

    private String region;

    private String basePath;

    private String domain;

    private String bucketName;

    private COSClient client;

    private COSClient getClient() {
        if (this.client == null) {
            // 设置用户身份信息。
            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            // ClientConfig 中包含了后续请求 COS 的客户端设置：
            ClientConfig clientConfig = new ClientConfig();
            // 设置 bucket 的地域
            // COS_REGION 请参照 https://cloud.tencent.com/document/product/436/6224
            clientConfig.setRegion(new Region(region));
            // 设置请求协议, http 或者 https
            clientConfig.setHttpProtocol(HttpProtocol.https);
            // 生成 cos 客户端。
            this.client = new COSClient(cred, clientConfig);
        }
        return this.client;
    }

    public void close() {
        if (this.client != null) {
            this.client.shutdown();
            this.client = null;
        }
    }

    @Override
    public FileInfo upload(FileInfo fileInfo, UploadPretreatment pre) {
        fileInfo.setBasePath(basePath);
        fileInfo.setUrl(domain + fileInfo.getFileKey());

        COSClient cosClient = this.getClient();
        try (InputStream in = pre.getFileWrapper().getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileInfo.getSize());
            metadata.setContentType(fileInfo.getContentType());
            cosClient.putObject(bucketName, fileInfo.getFileKey(), in, metadata);
        } catch (IOException e) {
            cosClient.deleteObject(bucketName, fileInfo.getFileKey());
            throw new FileStorageRuntimeException("文件上传失败！");
        }
        return fileInfo;
    }

    @Override
    public void delete(FileInfo fileInfo) {
        this.getClient().deleteObject(bucketName, fileInfo.getFileKey());
    }

    @Override
    public boolean exists(FileInfo fileInfo) {
        return this.getClient().doesObjectExist(bucketName, fileInfo.getFileKey());
    }

    @Override
    public void download(FileInfo fileInfo, Consumer<InputStream> consumer) {
        COSObject object = this.getClient().getObject(bucketName, fileInfo.getFileKey());
        try (InputStream in = object.getObjectContent()) {
            consumer.accept(in);
        } catch (IOException e) {
            throw new FileStorageRuntimeException("文件下载失败！platform: " + fileInfo.getPlatform());
        }
    }

}
