# 构建依赖说明

## 构建环境
- JDK: 21
- Gradle: 使用仓库内 `gradlew.bat`
- 推荐系统: Windows / Linux / macOS（默认构建不依赖个人绝对路径）

## 默认构建命令（团队共享）
- `./gradlew.bat clean shadowJar`
- `./gradlew.bat build`

默认构建仅负责打包，并将声明产物同步到仓库内 `target/`。

## 主要依赖项
- Maven 仓库依赖: `mavenCentral()`、`mavenLocal()` 以及工程内声明的远程仓库
- 可选本地仓库: 仅当设置 `localPluginRepoDir` 或 `LOCAL_PLUGIN_REPO_DIR` 时启用
- 跨模块依赖:
  - `:api`
  - `:backend`
  - `:platforms:bukkit:compatibility`
- `lib/` 目录依赖: 无硬性要求

## 产物名称
- `CustomNameplates.jar`（由 `:platforms:bukkit:shadowJar` 统一命名）
