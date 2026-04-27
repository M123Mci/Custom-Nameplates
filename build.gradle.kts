plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
}

val git : String = versionBanner()
val builder : String = builder()
ext["git_version"] = git
ext["builder"] = builder

/* ── 可选本地 Maven 仓库路径解析（共享构建默认不强依赖） ── */
val resolvedLocalPluginRepoDir: String? = (
    providers.gradleProperty("localPluginRepoDir").orNull
        ?: System.getenv("LOCAL_PLUGIN_REPO_DIR")
        ?: "C:/PluginLibs/Maven"
)
    ?.trim()
    ?.takeIf { it.isNotEmpty() }

if (resolvedLocalPluginRepoDir != null) {
    ext["resolvedLocalPluginRepoDir"] = resolvedLocalPluginRepoDir
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        if (resolvedLocalPluginRepoDir != null) {
            maven {
                name = "localPluginRepo"
                url = uri(file(resolvedLocalPluginRepoDir))
            }
        }
        mavenLocal()
        mavenCentral()
    }

    tasks.processResources {
        filteringCharset = "UTF-8"

        filesMatching(arrayListOf("custom-nameplates.properties")) {
            expand(rootProject.properties)
        }

        filesMatching(arrayListOf("*.yml", "*/*.yml")) {
            expand(
                mapOf(
                    "project_version" to rootProject.properties["project_version"]!!,
                    "config_version" to rootProject.properties["config_version"]!!,
                )
            )
        }
    }
}

/* ── 统一构建/发布约定 ── */
extra["unifiedPluginConfig"] = mapOf(
    "artifacts" to listOf(
        mapOf(
            "projectPath" to ":platforms:bukkit",
            "taskName" to "shadowJar",
            "fileName" to "CustomNameplates.jar",
            "groupId" to rootProject.properties["project_group"],
            "artifactId" to "CustomNameplates",
            "copyToDeploy" to true,
            "copyToBuildJars" to true,
            "publishToMaven" to true,
        ),
    ),
)

apply(from = rootProject.file("gradle/unified-plugin-conventions.gradle"))

fun versionBanner(): String = project.providers.exec {
    commandLine("git", "rev-parse", "--short=8", "HEAD")
}.standardOutput.asText.map { it.trim() }.getOrElse("Unknown")

fun builder(): String = project.providers.exec {
    commandLine("git", "config", "user.name")
}.standardOutput.asText.map { it.trim() }.getOrElse("Unknown")
