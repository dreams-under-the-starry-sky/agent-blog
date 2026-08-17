# 个人博客（三端）

同级目录：

- `BlogService`：Spring Boot 3 + MyBatis 后端（端口 8080）
- `Blog`：Vue3 前台（端口 5173）
- `BlogAdmin`：Vue3 后台（端口 5174）

使用云端已有库 `blogtest`，不新建数据库。

## 启动后端

必须使用 JDK 21（当前环境 `JAVA_HOME` 默认是 JDK 8，Spring Boot 3 无法用 Java 8 启动）。

```powershell
cd BlogService
.\start.ps1
```

或：

```powershell
$env:JAVA_HOME="E:\environment\Java\JDK\jdk21"
$env:Path="$env:JAVA_HOME\bin;" + $env:Path
& "E:\java\maven\apache-maven-3.6.3\bin\mvn.cmd" -s "E:\java\maven\apache-maven-3.6.3\conf\settings.xml" spring-boot:run
```

## 启动前台 / 后台

```powershell
cd Blog
npm install
npm run dev
```

```powershell
cd BlogAdmin
npm install
npm run dev
```

- 前台：http://localhost:5173
- 后台：http://localhost:5174
- 接口：http://localhost:8080/api/front/articles

后台账号为库中的 `admin`。本地密钥放在 `BlogService/src/main/resources/application-local.yml`（不入库），可复制 `application-local.yml.example`。若需重置密码，在 local 文件中设置 `blog.admin.reset-password` 后重启后端。
