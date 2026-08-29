# 推送到 GitHub 后自动部署

`main` 有新推送时，GitHub Actions 会构建三端并发布到 `124.222.86.239`：

| 产物 | 服务器目录 |
|------|------------|
| 前台 `Blog/dist` | `/www/wwwroot/agent-blog` |
| 后台 `BlogAdmin/dist` | `/www/wwwroot/agent-blog-admin` |
| `blog-service.jar`（当前运行副本） | `/www/wwwroot/springboot` |

构建会打上上海时区时间戳 `YYYYMMDD-HHMMSS`：

- 前台包 `blog-{时间戳}.tar.gz`、后台包 `blog-admin-{时间戳}.tar.gz` → `/www/wwwroot/springboot/releases/`（各保留最近 5 个）
- 后端 `blog-service-{时间戳}.jar` → `/www/wwwroot/springboot/`（再复制一份为 `blog-service.jar` 供启动；带时间戳的 jar 保留最近 5 个）
- CI 在 Ubuntu 上打包，jar 只含 Linux x64 的 FFmpeg 原生库（本机 Windows 打包仍只含 Windows 库）
- 大 jar 用 rsync 单独上传，避免和前台小文件挤在一次 scp 里卡住
- Actions 本次运行也可下载同名 artifact `agent-blog-{时间戳}`
- 站点根目录有 `build-stamp.txt`，内容即本次时间戳

后端在 `/www/wwwroot/springboot` 启动，读取**同目录** `application-local.yml`（CI **不会**上传或覆盖该文件）。端口 8080。

## 1. 服务器一次性准备

1. 安装 **JDK 21**（宝塔 Java 管理器即可），保证 `java -version` 为 21。
2. 确认目录存在：
   - `/www/wwwroot/springboot`
   - `/www/wwwroot/agent-blog`
   - `/www/wwwroot/agent-blog-admin`
3. 把现网 `application-local.yml` 放到 `/www/wwwroot/springboot/application-local.yml`。
4. 安装 rsync（构建机用 rsync 同步静态资源）：
   ```bash
   yum install -y rsync   # CentOS
   apt install -y rsync   # Debian/Ubuntu
   ```
5. 安全组 / 防火墙放行 **22**（给 GitHub Actions 用）。8080 建议只对本机 Nginx 开放。
6. Nginx 需已代理接口（与本地 Vite 一致，不要丢掉 `/api`）：
   ```nginx
   location /api/ {
       proxy_pass http://127.0.0.1:8080/agent-blog/server/api/;
       proxy_set_header Host $host;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-Forwarded-Proto $scheme;
   }
   location /uploads/ {
       proxy_pass http://127.0.0.1:8080/agent-blog/server/uploads/;
   }
   location /blog-manager/ {
       alias /www/wwwroot/agent-blog-admin/;
       try_files $uri $uri/ /blog-manager/index.html;
   }
   ```
   前台站点 root 指向 `/www/wwwroot/agent-blog`。后台 `base` 是 `/blog-manager/`，因此 `alias` 应对该目录里的 `index.html` 和 `assets/`。

## 2. SSH 密钥

在本机生成**仅用于部署**的密钥（不要用已有私人密钥）：

```bash
ssh-keygen -t ed25519 -C "github-deploy" -f deploy_key -N ""
```

把公钥写入服务器（用户按实际改，宝塔多为 `root`）：

```bash
ssh-copy-id -i deploy_key.pub root@124.222.86.239
```

或手动把 `deploy_key.pub` 追加到服务器 `~/.ssh/authorized_keys`。

## 3. GitHub Secrets

仓库 → Settings → Secrets and variables → Actions → New repository secret：

| Name | 值 |
|------|-----|
| `DEPLOY_SSH_KEY` | `deploy_key` **私钥**全文（含 `BEGIN` / `END`） |
| `DEPLOY_USER` | SSH 登录用户，如 `root` |

可选：

| Name | 默认 | 说明 |
|------|------|------|
| `DEPLOY_HOST` | `124.222.86.239` | 服务器 IP |
| `DEPLOY_PORT` | `22` | SSH 端口 |

不要把私钥提交进 Git。

## 4. 首次验证

1. 把 `.github/workflows/deploy.yml`、`deploy/start-backend.sh` 提交并推到 `main`，或在 Actions 里手动 Run workflow。
2. 打开仓库 **Actions**，等 Deploy 变绿。
3. 服务器检查：
   ```bash
   ss -lntp | grep 8080
   tail -n 50 /www/wwwroot/springboot/app.log
   ```
   日志里应出现 `Started BlogApplication`。
4. 浏览器打开前台、`/blog-manager/`、接口 `/agent-blog/server/api/front/articles`。

以后每次 `git push origin main` 都会再跑一遍：构建（产物带时间戳）→ 同步静态文件 → 覆盖 jar（不碰 yml）→ 重启 Java。

## 5. 失败时

- **Permission denied**：公钥没进 `authorized_keys`，或 `DEPLOY_USER` 不对。`DEPLOY_SSH_KEY` 必须是私钥全文（含 `BEGIN` / `END`），不要填 `.pub`。
- **SSH 卡住很久**：密钥未被接受时会在服务器上等密码；workflow 已设 `BatchMode`，会在约 20 秒内失败而不是挂死。
- **Host key verification**：一般由 workflow 里 `ssh-keyscan` 处理；若 IP 变了，重跑即可。
- **rsync: command not found**：服务器未装 rsync。
- **缺少 application-local.yml**：文件不在 `/www/wwwroot/springboot/`。
- **端口仍被占用**：`fuser -k 8080/tcp` 后再执行 `/www/wwwroot/springboot/start.sh`。
