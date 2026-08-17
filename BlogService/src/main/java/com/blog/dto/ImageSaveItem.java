package com.blog.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImageSaveItem {
    private String imgUrl;
    private String thumbnailUrl;

    public static List<ImageSaveItem> normalize(List<ImageSaveItem> images, List<String> urls) {
        if (images != null && !images.isEmpty()) {
            return images;
        }
        List<ImageSaveItem> result = new ArrayList<>();
        if (urls == null) {
            return result;
        }
        for (String url : urls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            ImageSaveItem item = new ImageSaveItem();
            item.setImgUrl(url);
            item.setThumbnailUrl(url);
            result.add(item);
        }
        return result;
    }
}
