package com.blog.storage;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.service.LogService;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "blog.storage.type", havingValue = "qiniu")
public class QiniuObjectStorage implements ObjectStorage {
    private static final Logger log = LoggerFactory.getLogger(QiniuObjectStorage.class);

    @Value("${blog.qiniu.access-key:}")
    private String accessKey;
    @Value("${blog.qiniu.secret-key:}")
    private String secretKey;
    @Value("${blog.qiniu.bucket:}")
    private String bucket;
    @Value("${blog.qiniu.cdn-domain:}")
    private String cdnDomain;
    @Value("${blog.qiniu.region:auto}")
    private String region;
    @Resource
    private LogService logService;

    private Auth auth;
    private UploadManager uploadManager;
    private BucketManager bucketManager;
    private String domain;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey) || !StringUtils.hasText(bucket) || !StringUtils.hasText(cdnDomain)) {
            throw new IllegalStateException(ErrorCode.QINIU_CONFIG_INCOMPLETE.getMessage());
        }
        Configuration cfg = new Configuration(resolveRegion(region));
        this.auth = Auth.create(accessKey.trim(), secretKey.trim());
        this.uploadManager = new UploadManager(cfg);
        this.bucketManager = new BucketManager(auth, cfg);
        this.domain = cdnDomain.trim();
    }

    @Override
    public String put(String key, byte[] data, String contentType) {
        try {
            String token = auth.uploadToken(bucket);
            Response resp = uploadManager.put(data, key, token, null, contentType, false);
            if (resp == null || !resp.isOK()) {
                String detail = resp == null
                        ? "key=" + key + " 七牛云返回为空"
                        : "key=" + key + " status=" + resp.statusCode + " " + resp.error;
                log.warn("七牛云上传失败 {}", detail);
                logService.recordFail("上传文件", detail);
                throw new BizException(ErrorCode.QINIU_UPLOAD_FAILED);
            }
            return publicUrl(key);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.warn("七牛云上传失败 key={}", key, e);
            logService.recordFail("上传文件", "key=" + key, e);
            throw new BizException(ErrorCode.QINIU_UPLOAD_FAILED, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            bucketManager.delete(bucket, key);
        } catch (Exception e) {
            log.warn("七牛云删除失败 key={}", key, e);
            logService.recordFail("删除文件", "key=" + key, e);
            throw new BizException(ErrorCode.QINIU_DELETE_FAILED, e);
        }
    }

    @Override
    public String publicUrl(String key) {
        return ObjectStorage.joinUrl(domain, key);
    }

    @Override
    public String extractKey(String url) {
        return ObjectStorage.stripPrefix(url, domain);
    }

    private static Region resolveRegion(String value) {
        String code = value == null ? "auto" : value.trim().toLowerCase();
        return switch (code) {
            case "z0", "huadong" -> Region.huadong();
            case "z1", "huabei" -> Region.huabei();
            case "z2", "huanan" -> Region.huanan();
            case "na0", "beimei" -> Region.beimei();
            case "as0", "xinjiapo", "singapore" -> Region.xinjiapo();
            default -> Region.autoRegion();
        };
    }
}
