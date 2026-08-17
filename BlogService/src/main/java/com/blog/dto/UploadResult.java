package com.blog.dto;

import lombok.Data;

@Data
public class UploadResult {
    private String url;
    private String thumbnailUrl;

    public static UploadResult of(String url, String thumbnailUrl) {
        UploadResult result = new UploadResult();
        result.setUrl(url);
        result.setThumbnailUrl(thumbnailUrl == null || thumbnailUrl.isBlank() ? url : thumbnailUrl);
        return result;
    }
}
