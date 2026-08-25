package com.blog.service;

import com.blog.common.BizException;
import com.blog.common.ErrorCode;
import com.blog.common.IdGenerator;
import com.blog.common.PageQuery;
import com.blog.common.PageResult;
import com.blog.dto.DashboardVO;
import com.blog.dto.UploadResult;
import com.blog.entity.Black;
import com.blog.entity.BlogLog;
import com.blog.entity.EmailRecord;
import com.blog.entity.FileDelFail;
import com.blog.entity.Friend;
import com.blog.entity.FriendCategory;
import com.blog.entity.Music;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.BlackMapper;
import com.blog.mapper.BlogLogMapper;
import com.blog.mapper.CommentMapper;
import com.blog.mapper.EmailRecordMapper;
import com.blog.mapper.FileDelFailMapper;
import com.blog.mapper.FriendCategoryMapper;
import com.blog.mapper.FriendMapper;
import com.blog.mapper.MessageMapper;
import com.blog.mapper.MusicMapper;
import com.blog.storage.ObjectStorage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MiscService {
    private static final Logger log = LoggerFactory.getLogger(MiscService.class);
    @Resource
    private FriendMapper friendMapper;
    @Resource
    private FriendCategoryMapper friendCategoryMapper;
    @Resource
    private MusicMapper musicMapper;
    @Resource
    private BlackMapper blackMapper;
    @Resource
    private BlogLogMapper blogLogMapper;
    @Resource
    private EmailRecordMapper emailRecordMapper;
    @Resource
    private FileDelFailMapper fileDelFailMapper;
    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private AvifCompressor avifCompressor;
    @Resource
    private ObjectStorage objectStorage;
    @Resource
    private LogService logService;

    @Value("${blog.storage.prefix:agent-blog}")
    private String storagePrefix;
    @Value("${blog.upload.dir:uploads}")
    private String uploadDir;
    @Value("${blog.upload.url-prefix:/uploads}")
    private String urlPrefix;

    private Path uploadRoot;

    @PostConstruct
    public void initUploadRoot() {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        log.info("当前文件存储: {}", objectStorage.getClass().getSimpleName());
    }

    public List<Friend> friends() {
        return friendMapper.selectAll();
    }

    public Long saveFriend(Friend friend) {
        if (friend.getCategoryId() != null) {
            FriendCategory category = friendCategoryMapper.selectById(friend.getCategoryId());
            if (category != null && category.getSort() != null) {
                friend.setSort(category.getSort());
            }
        }
        if (friend.getSort() == null) {
            friend.setSort(99);
        }
        if (friend.getId() == null) {
            friend.setId(IdGenerator.nextId());
            friendMapper.insert(friend);
        } else {
            friendMapper.update(friend);
        }
        return friend.getId();
    }

    public void deleteFriend(Long id) {
        friendMapper.deleteById(id);
    }

    public List<FriendCategory> friendCategories() {
        return friendCategoryMapper.selectAll();
    }

    public Long saveFriendCategory(FriendCategory category) {
        category.setName(category.getName().trim());
        if (friendCategoryMapper.countByName(category.getName(), category.getId()) > 0) {
            throw new BizException(ErrorCode.CATEGORY_NAME_EXISTS);
        }
        if (category.getSort() == null) {
            category.setSort(99);
        }
        if (category.getId() == null) {
            category.setId(IdGenerator.nextId());
            friendCategoryMapper.insert(category);
        } else {
            friendCategoryMapper.update(category);
        }
        return category.getId();
    }

    public void deleteFriendCategory(Long id) {
        if (friendMapper.countByCategoryId(id) > 0) {
            throw new BizException(ErrorCode.CATEGORY_HAS_FRIENDS);
        }
        friendCategoryMapper.deleteById(id);
    }

    public List<Music> musicList() {
        return musicMapper.selectAll();
    }

    public Long saveMusic(Music music) {
        music.setName(music.getName().trim());
        music.setAuthor(trimToNull(music.getAuthor()));
        music.setUrl(music.getUrl().trim());
        music.setCover(trimToNull(music.getCover()));
        music.setLrc(trimToNull(music.getLrc()));
        if (music.getId() == null) {
            music.setId(IdGenerator.nextId());
            musicMapper.insert(music);
        } else {
            musicMapper.update(music);
        }
        return music.getId();
    }

    public void deleteMusic(Long id) {
        musicMapper.deleteById(id);
    }

    public List<Black> blacks() {
        return blackMapper.selectAll();
    }

    public Integer saveBlack(Black black) {
        if (black.getId() == null) {
            black.setId((int) (System.currentTimeMillis() / 1000));
        }
        blackMapper.insert(black);
        return black.getId();
    }

    public void deleteBlack(Integer id) {
        blackMapper.deleteById(id);
    }

    public PageResult<BlogLog> logs(PageQuery query) {
        return new PageResult<>(blogLogMapper.countPage(query), blogLogMapper.selectPage(query));
    }

    public PageResult<EmailRecord> emails(PageQuery query) {
        return new PageResult<>(emailRecordMapper.countPage(query), emailRecordMapper.selectPage(query));
    }

    public List<FileDelFail> fileDelFails() {
        return fileDelFailMapper.selectAll();
    }

    public void deleteFileDelFail(Integer id) {
        fileDelFailMapper.deleteById(id);
    }

    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setArticleCount(articleMapper.countAll());
        vo.setFriendCount(friendMapper.countAll());
        vo.setMessageCount(messageMapper.countAll());
        vo.setCommentCount(commentMapper.countAll());
        vo.setBlackCount(blackMapper.countAll());
        vo.setErrorLogCount(blogLogMapper.countFailed());
        vo.setHotArticles(articleMapper.selectHot(10));
        vo.setRecentBlacks(blackMapper.selectSince(LocalDate.now().minusDays(1).atStartOfDay()));
        return vo;
    }

    public UploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.FILE_REQUIRED);
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        String stem = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String name = stem + ext;
        String key = objectKey(name);
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            log.warn("读取上传文件失败", e);
            logService.recordFail("上传文件", e);
            throw new BizException(ErrorCode.FILE_READ_FAILED, e);
        }
        String url = clipUrl(objectStorage.put(key, bytes, contentType(file, ext)));
        String thumbnailUrl = url;
        if (isGif(ext, bytes)) {
            return UploadResult.of(url, url);
        }
        String avifName = ".avif".equals(ext) ? stem + "_t.avif" : stem + ".avif";
        byte[] avif = avifCompressor.compressToAvif(bytes);
        if (avif != null && avif.length > 0) {
            thumbnailUrl = clipUrl(objectStorage.put(objectKey(avifName), avif, "image/avif"));
        }
        return UploadResult.of(url, thumbnailUrl);
    }

    private static boolean isGif(String ext, byte[] bytes) {
        if (".gif".equals(ext)) {
            return true;
        }
        if (bytes == null || bytes.length < 6) {
            return false;
        }
        return bytes[0] == 'G'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == '8'
                && (bytes[4] == '7' || bytes[4] == '9')
                && bytes[5] == 'a';
    }

    public void tryDeleteFiles(String... urls) {
        if (urls == null) {
            return;
        }
        for (String url : urls) {
            tryDeleteFile(url);
        }
    }

    public void tryDeleteFile(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        try {
            String key = objectStorage.extractKey(url);
            if (StringUtils.hasText(key)) {
                objectStorage.delete(key);
                return;
            }
            deleteLegacyLocal(url);
        } catch (Exception e) {
            log.warn("删除文件失败 url={}", url, e);
            String extra = e instanceof BizException biz ? biz.getErrorCode().getMessage() : ErrorCode.FILE_DELETE_FAILED.getMessage();
            logService.recordFail("删除文件", url, e);
            FileDelFail fail = new FileDelFail();
            fail.setFileKey(url.length() > 60 ? url.substring(0, 60) : url);
            fail.setExtra(extra);
            fileDelFailMapper.insert(fail);
        }
    }

    private void deleteLegacyLocal(String url) throws IOException {
        if (!url.startsWith(urlPrefix)) {
            return;
        }
        String relative = url.substring(urlPrefix.length());
        if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        Path file = uploadRoot.resolve(relative).normalize();
        if (!file.startsWith(uploadRoot)) {
            return;
        }
        Files.deleteIfExists(file);
    }

    private String objectKey(String filename) {
        LocalDate now = LocalDate.now();
        int quarter = (now.getMonthValue() - 1) / 3 + 1;
        String prefix = StringUtils.hasText(storagePrefix) ? storagePrefix.trim() : "agent-blog";
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + "/" + now.getYear() + "/Q" + quarter + "/" + filename;
    }

    private static String contentType(MultipartFile file, String ext) {
        String type = file.getContentType();
        if (StringUtils.hasText(type) && !"application/octet-stream".equals(type)) {
            return type;
        }
        return switch (ext) {
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".avif" -> "image/avif";
            case ".svg" -> "image/svg+xml";
            case ".mp3" -> "audio/mpeg";
            default -> "application/octet-stream";
        };
    }

    public String clipUrl(String url) {
        if (url == null) {
            return null;
        }
        return url.length() > 80 ? url.substring(0, 80) : url;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
