package com.campus.lostfound.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务
 */
public interface FileService {

    /**
     * 上传图片
     * @param file 图片文件
     * @return 图片访问URL
     */
    String uploadImage(MultipartFile file);
}
