package com.boring.community.until;


/**
 * 判断文件是否为图片<br>
 */

import org.springframework.stereotype.Component;

@Component
public class IsPictureUtil {

    public static boolean IsPicture(String suffix) {
        // 文件名称为空的场合
        if (suffix.isBlank()) {
            // 返回不和合法
            return true;
        }
        // 声明图片后缀名数组
        String[] imageArray = {
                "bmp","dib", "gif",
                "jfif" , "jpe", "jpeg",
                "jpg", "png" ,"tif",
                "tiff", "ico"
        };
        // 遍历名称数组
        for (int i = 0; i < imageArray.length; i++) {
            // 判断单个类型文件的场合
            if (imageArray[i].equals(suffix)) {
                return false;
            }
        }
        return true;
    }
}