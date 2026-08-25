package com.blog.common;

import org.springframework.util.StringUtils;

public final class MarkdownExcerpt {
    private MarkdownExcerpt() {
    }

    public static String from(String markdown, int maxLen) {
        if (maxLen <= 0 || !StringUtils.hasText(markdown)) {
            return "";
        }
        String text = markdown;
        text = text.replaceAll("(?s)<!--.*?-->", " ");
        text = text.replaceAll("(?s)```.*?```", " ");
        text = text.replaceAll("(?s)~~~.*?~~~", " ");
        text = text.replaceAll("!\\[[^\\]]*\\]\\([^)]*\\)", " ");
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^)]*\\)", "$1");
        text = text.replaceAll("\\[([^\\]]+)\\]\\[[^\\]]*\\]", "$1");
        text = text.replaceAll("(?m)^\\s*\\[[^\\]]+\\]:\\s+\\S+.*$", " ");
        text = text.replaceAll("`([^`]+)`", "$1");
        text = text.replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", "");
        text = text.replaceAll("(?m)^\\s{0,3}>\\s?", "");
        text = text.replaceAll("(?m)^\\s{0,3}(-{3,}|\\*{3,}|_{3,}|={3,})\\s*$", " ");
        text = text.replaceAll("(?m)^\\s*([-*+]|\\d+\\.)\\s+", "");
        text = text.replaceAll("[*]{1,3}|[_]{1,3}|~{2}", "");
        text = text.replaceAll("<[^>]+>", " ");
        text = text.replace("&nbsp;", " ").replaceAll("&[#a-zA-Z0-9]+;", " ");
        text = text.replace('\u00A0', ' ').replaceAll("[\\s\\u3000]+", " ").trim();
        if (text.length() > maxLen) {
            return text.substring(0, maxLen-3).trim() + "...";
        }
        return text;
    }
}
