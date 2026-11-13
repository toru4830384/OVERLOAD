# ビルド中の依存関係と実行ファイルを分離し、実行中のJARを更新しない。
FROM node:26-bookworm-slim AS frontend
WORKDIR /frontend
COPY package.json package-lock.json ./
RUN npm ci
COPY index.html vite.config.js ./
COPY public ./public
COPY src ./src
RUN npm run build

# 検証済みのGradleを含む公式イメージで、毎回の配布ZIP取得への依存をなくす。
FROM gradle:9.7.1-jdk17-jammy AS backend
WORKDIR /source/backend
COPY backend/gradlew backend/gradlew.bat ./
COPY backend/gradle ./gradle
COPY backend/build.gradle backend/settings.gradle ./
COPY backend/src ./src
COPY --from=frontend /frontend/dist /source/frontend-dist
RUN gradle --no-daemon bootJar -PfrontendDist=/source/frontend-dist

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --system overload && useradd --system --gid overload --home-dir /app overload
COPY --from=backend --chown=overload:overload /source/backend/build/libs/overload-api-0.0.1-SNAPSHOT.jar /app/app.jar
USER overload
# 無料枠のメモリーに合わせる。実機でRSS・同時要求・再起動を公開前に確認する。
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx192m -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=48m -Xss512k -XX:ActiveProcessorCount=1 -XX:+ExitOnOutOfMemoryError"
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=render"]
