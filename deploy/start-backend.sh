#!/bin/bash
# 在 /www/wwwroot/springboot 下启动后端。
# 读取同目录 application-local.yml，不覆盖该文件。
set -euo pipefail

APP_DIR=/www/wwwroot/springboot
JAR="$APP_DIR/blog-service.jar"
LOG="$APP_DIR/app.log"
PID_FILE="$APP_DIR/app.pid"
LOCAL_YML="$APP_DIR/application-local.yml"

if [ -z "${JAVA_HOME:-}" ]; then
  for d in /www/server/java/jdk-21* /usr/lib/jvm/java-21* /usr/lib/jvm/jdk-21*; do
    if [ -x "${d}/bin/java" ]; then
      export JAVA_HOME="$d"
      break
    fi
  done
fi
JAVA_CMD="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if ! command -v "$JAVA_CMD" >/dev/null 2>&1 && [ ! -x "$JAVA_CMD" ]; then
  JAVA_CMD=java
fi

if [ ! -f "$JAR" ]; then
  echo "缺少 $JAR" >&2
  exit 1
fi
if [ ! -f "$LOCAL_YML" ]; then
  echo "缺少 $LOCAL_YML，请先把配置放到 jar 同目录" >&2
  exit 1
fi

stop_old() {
  if [ -f "$PID_FILE" ]; then
    old="$(cat "$PID_FILE" 2>/dev/null || true)"
    if [ -n "${old}" ] && kill -0 "$old" 2>/dev/null; then
      kill "$old" || true
      for _ in 1 2 3 4 5; do
        kill -0 "$old" 2>/dev/null || break
        sleep 1
      done
      kill -9 "$old" 2>/dev/null || true
    fi
    rm -f "$PID_FILE"
  fi
  if command -v fuser >/dev/null 2>&1; then
    fuser -k 8080/tcp >/dev/null 2>&1 || true
  fi
}

cd "$APP_DIR"
stop_old
sleep 1

nohup "$JAVA_CMD" -jar "$JAR" \
  --spring.config.additional-location="file:${APP_DIR}/" \
  --spring.config.import="optional:file:${LOCAL_YML}" \
  >> "$LOG" 2>&1 &
echo $! > "$PID_FILE"
echo "已启动 pid=$(cat "$PID_FILE") jar=$JAR 日志 $LOG"
