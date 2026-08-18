package com.lifeshop.controller;

import com.lifeshop.common.result.Result;
import com.lifeshop.service.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传控制器
 * 使用阿里云OSS进行云端文件存储
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final OssService ossService;

    /**
     * 上传店铺Logo
     * 存储到OSS的logos目录
     */
    @PostMapping("/logo")
    public Result<String> uploadLogo(@RequestParam("file") MultipartFile file) {
        try {
            String url = ossService.uploadImage(file, "logos");
            return Result.success(url);
        } catch (IllegalArgumentException e) {
            return Result.fail(com.lifeshop.common.result.ResultCode.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            return Result.fail(com.lifeshop.common.result.ResultCode.BAD_REQUEST, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传用户头像
     * 存储到OSS的avatars目录
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String url = ossService.uploadImage(file, "avatars");
            return Result.success(url);
        } catch (IllegalArgumentException e) {
            return Result.fail(com.lifeshop.common.result.ResultCode.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            return Result.fail(com.lifeshop.common.result.ResultCode.BAD_REQUEST, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传商品图片
     * 存储到OSS的goods目录
     */
    @PostMapping("/goods")
    public Result<String> uploadGoods(@RequestParam("file") MultipartFile file) {
        try {
            String url = ossService.uploadImage(file, "goods");
            return Result.success(url);
        } catch (IllegalArgumentException e) {
            return Result.fail(com.lifeshop.common.result.ResultCode.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            return Result.fail(com.lifeshop.common.result.ResultCode.BAD_REQUEST, "文件上传失败: " + e.getMessage());
        }
    }
}
