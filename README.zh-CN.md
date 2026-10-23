# WhereRebirth

简体中文 | [English](README.md)

WhereRebirth 是一个从已停止维护的旧项目恢复而来的 Android 本地生活应用。服务器、推送与分享服务均已停用，现在作为可离线运行的本机演示项目保留，用于学习、兼容性研究和纪念。

## 演示模式

- **登录**：任意非空手机号和密码（或验证码）均可通过，界面会提示本机演示模式不校验账号。
- **接口数据**：Retrofit 请求由 `app/src/main/assets/mock/api/` 下的 JSON 本地响应，不再访问旧服务器。
- **图片**：首页横幅、美食、酒店、旅游、商城、招聘、新闻和头像均使用 `drawable-nodpi/` 下的本地图片。
- **第三方登录/分享**：Facebook/Google 登录返回本地演示账号；分享使用系统分享面板。
- **推送**：已移除 JPush/JCore 及 Mob ShareSDK，不再初始化任何商业推送/分享 SDK。
- **地图**：地图页面仍在项目中，实时地图瓦片需要配置自己的 Google Maps Key。

## 编译

```bash
./gradlew :app:assembleDevDebug
```

工具链：Gradle 8.13、AGP 8.13.0、Kotlin 2.0.21、Java/JVM 17、compileSdk 36、minSdk 29。

Gradle 8.13 不能在 Java 25 上运行，请把 Gradle JDK 设为 JDK 17。

## 本地 API Mock

旧服务器已停止，项目通过本地 JSON 满足 API 请求，同时保留原来的 Retrofit、RxJava 和 `BaseObserver` 调用链。

### 相关文件

- `app/src/main/assets/mock/api/`：每个 `RetrofitApi` 方法对应一份响应 JSON。
- `app/src/main/assets/mock/api/_index.json`：Retrofit 方法名、JSON 文件名和返回类型的映射。
- `app/src/main/java/com/jiechengsheng/city/api/LocalMockInterceptor.kt`：读取 JSON 并构造本地 HTTP 响应。
- `app/src/main/java/com/jiechengsheng/city/api/MockJsonFactory.kt`：按 JavaBean 类型生成兜底 JSON。
- `app/src/test/java/com/jiechengsheng/city/api/GenerateApiMocksTest.kt`：重新生成全部 JSON。
- `app/src/test/java/com/jiechengsheng/city/api/ValidateApiMocksTest.kt`：校验每份 JSON 是否能按声明的 JavaBean 解析。

运行时，`LocalMockInterceptor` 会先用方法声明的返回类型校验资产 JSON。文件缺失或类型不兼容时自动使用生成的兜底数据，避免手写 JSON 进入 Retrofit 转换器后才抛异常。

同名重载接口会在文件名后附加参数类型，防止文件冲突。

### 校验全部 JSON

```bash
./gradlew :app:testDevDebugUnitTest --tests "com.jiechengsheng.city.api.ValidateApiMocksTest"
```

### 重新生成全部 JSON

新增接口或修改响应 JavaBean 后运行。**这会覆盖手工编辑的 JSON（包括首页数据）**。

```bash
./gradlew :app:testDevDebugUnitTest --tests "com.jiechengsheng.city.api.GenerateApiMocksTest" --rerun-tasks
```

生成器会为每个接口生成完整的 `JcsResponse<T>`，集合与分页默认 5 条数据，并根据字段类型生成合理的 ID、状态、名称和本地图片引用。重新生成后如需保留首页、招聘、新闻等定制数据，请重新应用对应 JSON 的修改。

### 修改某个接口

1. 在 `app/src/main/assets/mock/api/_index.json` 中找到接口方法。
2. 打开对应的 JSON 文件。
3. 保留 `JcsResponse<T>` 需要的 `code`、`message`、`data` 外层结构。
4. 保留 `module_type`、`content_type`、状态值和 ID 等业务关键字段。
5. 运行上面的校验测试并编译。

本地图片使用 `android.resource://com.jiechengsheng.city/drawable/<name>` 或 `.../mipmap/<name>` 引用。

### 演示图片与应用图标

- `app/src/main/res/drawable-nodpi/`：压缩后的演示照片与 `mock_logo.png`。
- `app/src/main/res/drawable/ic_module_*.xml`：首页金刚区矢量图标。
- `app/src/main/res/drawable/ic_launcher_foreground.xml`、`ic_launcher_background.xml`：自适应启动图标。
- `app/src/main/res/mipmap-anydpi-v26/ic_app_logo*.xml`：桌面图标入口。

## 已移除的商业组件

- JPush/JCore：依赖、Manifest 中的 Service/Receiver/Provider、`libjcore*.so` 全部移除。
- Mob ShareSDK / MobSDK Gradle 插件：移除；分享改用系统分享，第三方登录改为本机模拟。
- Facebook SDK 与 Facebook App Links：移除。
- 饺子播放器/ijkplayer：改用项目已有的 GSYVideoPlayer，酒店媒体页不受影响。APK 中剩余的 `libijk*.so` 是 GSY 的播放内核；完全不需要视频时可再移除 GSY 依赖。

## 本地配置与凭据

仓库不包含生产环境凭据、私人账号、API Token、密码或个人联系方式。需要真实 Google Maps 时请在本地配置自己的 Key。离线 Mock 模式不需要后端服务。

## 许可证与归属

原项目及其依赖可能具有各自的版权和许可证条款。本仓库不声明拥有旧项目源代码，也未提供统一的开源许可证。重新分发前请确认授权并添加合适的许可证。
