package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/admin/common/upload")
@Api(tags = "文件上传")
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传...");
        try {
            String originalFilename = file.getOriginalFilename(); //获取文件 的全名
            String extend = originalFilename.substring(originalFilename.lastIndexOf("."));  //获取文件的扩展名
            String imageName = UUID.randomUUID() + extend;  //生成UUID
            String uploadName = aliOssUtil.upload(file.getBytes(), imageName);
            return Result.success(uploadName);
        } catch (IOException e) {
            log.error("文件上传失败:{}",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
