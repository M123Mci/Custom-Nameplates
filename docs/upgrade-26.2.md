# CustomNameplates 26.2 合并与验收记录

## 源码与构建

- 接管基线：`57a460a6d92433693df164f984950e992bef088c`。
- 官方 main：`22e1ef21ea0351fa2ecb913dc377145e37bca48a`。
- 共同祖先：`0387b301495a18fbb1c80e73fe29f8004b019f46`；使用正常三方合并，逐项解决版本、追踪时序和名称可见性冲突。
- 版本：`3.0.44-mr.1`；Java 25；Paper `26.2.build.121-stable`。
- `:api:compileJava`、`:backend:compileJava`、`:platforms:bukkit:compileJava`、最终 `clean shadowJar` 均通过。
- 产物：`jars/CustomNameplates.jar`；本地发布插件坐标：`net.momirealms:CustomNameplates:3.0.44-mr.1`，路径 `../../PluginLibs/Maven`。

## 保留清单

| 接管能力 | 合并结果 | 验收 |
| --- | --- | --- |
| `ExternalPassengerProvider`／`ExternalPassengerRegistry` | 保留注册、注销、查询和生命周期接口 | 源码保留，下游编译通过 |
| `TagRenderer.refreshPassengersForViewers()` | 保留外部背包乘客合并与主动刷新 | 源码保留，视觉待验 |
| `UnlimitedTagManager.refreshPassengers(CNPlayer)` | 保留 MagicCosmetic 主动刷新入口 | 下游编译通过 |
| 玩家 scale、crouching、spectator 初始状态 | 与官方 `afterSend` 顺序修复共同保留 | 源码检查通过，玩家待验 |
| 自定义配置、样式、占位符、聊天提供者、身份和权限 | 保留原实现与资源标识，不以缺依赖删功能 | 编译通过；原有自定义数据回归待验 |
| 单版本实现 | 固定 26.2 名称可见性字段、NBT 处理与版本检查；不恢复旧版本分派 | Spawn 启动通过 |

补充修复：`PacketEvent` 执行回调后释放任务列表；取消出站数据包时完成 promise。接管 `isVersion26_1_2()` 方法仍保留，返回其实际版本判断结果；新目标为 26.2。

保留 BubbleConfigImpl、BubbleTag 的原构造方法；旧气泡配置缺少 `duration-per-character` 时维持固定显示时间，不自动增加时长。新安装默认配置可以显式启用按字数增加时长。

保留 `SenderFactory.getAudience()` 及 Bukkit 实现；新增气泡接口方法提供原行为对应的默认值，避免已有扩展实现出现 `AbstractMethodError`。

## 依赖与资源包

旧机器绝对路径已移除。原接管源码删除、但聊天联动仍依赖的二进制 API，从此次官方 Git 提交内取回到 `../../PluginLibs/Jars`；未生成替代 API。ItemsAdder 原仓库返回 HTTP 530，改用作者当前 Maven Central 坐标 `beer.devs:itemsadder-api:4.0.17`，联动代码编译通过。其余插件专属仓库按提供的依赖组过滤，避免失效仓库阻断无关依赖。

资源包只声明格式 `[88, 0]`，使用 `min_format`／`max_format`，对应本机 26.2 客户端 `version.json`。依据：[26.2 官方版本说明](https://feedback.minecraft.net/hc/en-us/articles/46690753273997-Minecraft-Java-Edition-26-2)、[新版资源包元数据规则](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-9)。不再使用旧 `pack_1_21_4.mcmeta` 模板。

## 验收边界

| 项目 | 结果 |
| --- | --- |
| Spawn 启动、动态依赖加载、CraftEngine 挂钩 | 已验证 |
| 平台控制台执行 `nameplates reload` | 已验证：配置重载和资源生成成功 |
| 生成资源包 | 已验证：300 个条目、4 个 JSON 正常解析，元数据为 88.0 |
| MagicCosmetic common 对新插件 JAR 编译 | 已验证；未改动其已有工作区修改 |
| 中文字体实际渲染、名称板／气泡、占位符、旁观者、时装、重登恢复 | 待客户端与实际业务配置回归 |
| 原有自定义样式和持久化数据迁移 | 本次 Spawn 原先未安装该插件，没有原业务数据可供回归 |

因此本记录不宣称完整升级验收完成。此次使用生成的默认本地存储验证启动，不把已有数据迁往新数据库，不写入数据库密码。

## 回退

没有变更持久化数据格式；资源包模板更新后重新生成即可。通过 MagicPlatform 正常停服后恢复旧 JAR 与对应配置／数据备份。此次 Spawn 是首次安装；如需撤回测试部署，将插件 JAR 和本次新建数据目录移至备份即可。其余实例未部署。
