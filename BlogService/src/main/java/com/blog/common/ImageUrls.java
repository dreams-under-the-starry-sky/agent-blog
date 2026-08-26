package com.blog.common;

import com.blog.dto.ImageSaveItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ImageUrls {
    private ImageUrls() {
    }

    public static String clip(String url) {
        if (url == null) {
            return null;
        }
        return url.length() > 80 ? url.substring(0, 80) : url;
    }

    public static Set<String> keepSet(List<ImageSaveItem> next, String... extras) {
        Set<String> keep = new HashSet<>();
        if (extras != null) {
            for (String extra : extras) {
                if (extra != null) {
                    keep.add(extra);
                }
            }
        }
        if (next == null) {
            return keep;
        }
        for (ImageSaveItem item : next) {
            if (item.getImgUrl() != null) {
                keep.add(item.getImgUrl());
            }
            if (item.getThumbnailUrl() != null) {
                keep.add(item.getThumbnailUrl());
            }
        }
        return keep;
    }

    public static void addUnused(List<String> urls, String imgUrl, String thumbnailUrl, Set<String> keep) {
        if (imgUrl != null && !keep.contains(imgUrl)) {
            urls.add(imgUrl);
        }
        if (thumbnailUrl != null && !keep.contains(thumbnailUrl)) {
            urls.add(thumbnailUrl);
        }
    }
}
