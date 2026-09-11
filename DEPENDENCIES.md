# 构建依赖说明

- 构建工具链与字节码：Java 25；Paper API：26.2.build.121-stable。
- 使用仓库 Gradle wrapper：`gradlew.bat clean shadowJar`。
- 产物：`jars/CustomNameplates.jar`。
- 构建完成后按真实项目版本发布至 `../../PluginLibs/Maven`；可通过 `localPluginRepoDir` 或 `LOCAL_PLUGIN_REPO_DIR` 指定目录。
- 聊天插件二进制 API 放在 `../../PluginLibs/Jars`，其余依赖从构建文件指定的 Maven 仓库解析。不得用空类替代缺失 API。
- 模块依赖：api → backend → platforms:bukkit:compatibility → platforms:bukkit。
- 服务器控制统一使用 MagicPlatform，不能以独立 Java 进程替代管理平台。

完整源码基线、依赖来源、自定义保留清单与未验证项目见 [升级记录](docs/upgrade-26.2.md)。
