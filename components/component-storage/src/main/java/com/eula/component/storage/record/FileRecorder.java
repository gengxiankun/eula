package com.eula.component.storage.record;

import com.eula.component.storage.FileInfo;

/**
 * 文件记录者接口
 * @author xiankun.geng
 */
public interface FileRecorder {

    /**
     * 保存文件记录
     */
    boolean record(FileInfo fileInfo);

    /**
     * 根据 url 获取文件记录
     */
    FileInfo getByUrl(String url);

    /**
     * 根据 url 删除文件记录
     */
    boolean delete(String url);

}
