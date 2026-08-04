# WhereRebirth

简体中文 | [English](README.md)

WhereRebirth 是一个从已停止维护的旧项目恢复而来的 Android 本地生活应用。目前主要用于学习、兼容性研究和作品展示，可在原服务器已经停止的情况下离线运行。

## 离线恢复说明

- Retrofit 请求会在访问已停用服务器之前被本地拦截。
- `RetrofitApi` 中的每个方法都在 `app/src/main/assets/mock/api/` 下拥有可编辑的 JSON 响应文件。
- `LocalMockInterceptor` 根据 Retrofit 方法签名精确选择对应 JSON。
- 如果 JSON 文件缺失，会按照接口声明的 JavaBean 返回类型自动生成兼容响应作为兜底。
- 列表和 `PageResponse<T>` 分页响应默认生成约 5 条数据。
- 保留原有 Retrofit Converter、RxJava、`BaseObserver` 和 `onError` 调用链。
- 远程图片字段默认替换为应用内置图标。
- Google Maps SDK 和地图相关页面仍保留在项目中。
- Google 和 Facebook 授权在离线预览模式下模拟为成功结果。

## 编译

```powershell
.\gradlew.bat :app:assembleDevDebug
```

项目使用 Gradle 7.5，需要使用兼容的 Gradle JVM，推荐使用 JDK 17。

## 本地 API Mock

旧服务器已经停止，因此项目通过本地 JSON 满足 API 请求，同时保留原来的 Retrofit 和 RxJava 请求流程。

### 相关文件

- `app/src/main/assets/mock/api/`：每个 `RetrofitApi` 方法对应一份响应 JSON。
- `app/src/main/assets/mock/api/_index.json`：记录 Retrofit 方法名、JSON 文件名和返回类型的映射。
- `app/src/main/java/com/jiechengsheng/city/api/LocalMockInterceptor.kt`：读取 JSON 并构造本地 HTTP 响应。
- `app/src/main/java/com/jiechengsheng/city/api/MockJsonFactory.kt`：根据 JavaBean 类型生成默认 JSON。
- `app/src/test/java/com/jiechengsheng/city/api/GenerateApiMocksTest.kt`：重新生成并校验全部接口 JSON。

如果存在同名重载接口，生成器会在文件名后附加参数类型，防止文件冲突。运行时如果找不到对应文件，不会访问远程服务器，而是使用 JavaBean 生成器返回兜底数据。

### 重新生成全部 JSON

新增接口或修改响应 JavaBean 后运行：

```powershell
.\gradlew.bat :app:testDevDebugUnitTest --tests "com.jiechengsheng.city.api.GenerateApiMocksTest" --rerun-tasks
```

生成器会：

- 为全部 `RetrofitApi` 方法生成完整的 `JcsResponse<T>` JSON。
- 为集合和分页数据默认生成约 5 条记录。
- 解析 `PageResponse<T>` 等嵌套泛型。
- 自动填写 ID、图片、经纬度、时间和部分类型字段。
- 校验生成的每个文件是否为合法 JSON。

### 修改某个接口的响应

1. 在 `app/src/main/assets/mock/api/_index.json` 中查找接口方法。
2. 打开索引中记录的 JSON 文件。
3. 保留 `JcsResponse<T>` 所需的 `code`、`message` 和 `data` 外层结构。
4. 注意保留 `module_type`、列表类型、状态值和 ID 等业务关键字段。
5. 重新编译并运行，无需启动后端服务。

自动生成的数据主要用于保证结构完整和页面能够开始运行。对于业务规则复杂的页面，仍可能需要手动调整 JSON 中的状态、类型和关联 ID。测试错误流程时，可以修改对应 JSON 的 `code` 和 `message`，或者调整 Interceptor 返回的 HTTP 状态码。

## 本地配置与安全

仓库不应包含生产环境凭据、私人账号、API Token、密码或个人联系方式。如果需要使用真实的 Google Maps 或其他第三方服务，请在本地配置自己的密钥。离线 Mock 模式不需要可用的后端服务器。

## 许可证与归属

原项目及其依赖可能具有各自的版权和许可证条款。本仓库不声明拥有旧项目源代码，也未提供统一的开源许可证。在重新分发前，请先确认相关授权并添加适当的许可证。
