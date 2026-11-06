# WhereRebirth

[简体中文](README.zh-CN.md) | English

WhereRebirth is an offline-restored Android local-life application based on a discontinued legacy project. It runs as a self-contained demo for learning, compatibility research, and portfolio demonstration.

## Demo mode

The original backend and push/share services are retired. Everything needed by the demo is bundled locally:

- **Login:** any non-empty account and password (or verification code) succeeds. The UI shows that the account is not verified on this device.
- **API data:** Retrofit requests are answered by editable JSON fixtures under `app/src/main/assets/mock/api/`.
- **Images:** banners, food, hotels, travel, mall, jobs, news, and avatars use bundled local images in `app/src/main/res/drawable-nodpi/`.
- **Third-party login/share:** Facebook and Google buttons return a local demo account; sharing opens the system share sheet.
- **Push:** JPush/JCore and the Mob ShareSDK were removed. No push service or commercial share SDK is initialized.
- **Maps:** Google Maps screens remain in the project but require your own API key for live map tiles.

## Build

```bash
./gradlew :app:assembleDevDebug
```

Toolchain: Gradle 8.13, AGP 8.13.0, Kotlin 2.0.21, Java/JVM 17, compileSdk 36, minSdk 29.

Gradle 8.13 does not run on Java 25. Use JDK 17 for the Gradle JVM.

## Local API mock data

The legacy backend is no longer available, so API requests are satisfied locally while retaining the original Retrofit, RxJava, and `BaseObserver` call chain.

### Files

- `app/src/main/assets/mock/api/`: one response fixture per `RetrofitApi` method.
- `app/src/main/assets/mock/api/_index.json`: mapping between Retrofit method names, fixture filenames, and return types.
- `app/src/main/java/com/jiechengsheng/city/api/LocalMockInterceptor.kt`: loads fixture files and creates local HTTP responses.
- `app/src/main/java/com/jiechengsheng/city/api/MockJsonFactory.kt`: generates fallback JSON from JavaBean types.
- `app/src/test/java/com/jiechengsheng/city/api/GenerateApiMocksTest.kt`: regenerates every fixture.
- `app/src/test/java/com/jiechengsheng/city/api/ValidateApiMocksTest.kt`: verifies that every fixture parses against its declared JavaBean type.

At runtime, `LocalMockInterceptor` first checks whether the selected fixture parses as the method's declared return type. Missing or incompatible fixtures fall back to generated JSON, so a malformed hand-edited fixture cannot reach Retrofit's converter.

Overloaded Retrofit methods use a parameter-type suffix in the filename to prevent collisions.

### Validate fixtures

```bash
./gradlew :app:testDevDebugUnitTest --tests "com.jiechengsheng.city.api.ValidateApiMocksTest" --rerun-tasks
```

`--rerun-tasks` matters: Gradle does not track `src/main/assets` as a test input, so after hand-editing a fixture the task can report `UP-TO-DATE` and skip validation entirely, returning a stale pass. Always force the re-run when fixtures changed.

### Regenerate all fixtures

Run this after adding an API method or changing a response JavaBean. **This overwrites hand-edited fixtures**, including the home-page data.

```bash
./gradlew :app:testDevDebugUnitTest --tests "com.jiechengsheng.city.api.GenerateApiMocksTest" --rerun-tasks
```

The generator creates complete `JcsResponse<T>` payloads, five records for collection and pagination responses, and type-aware sample values (ids, status flags, names, local image resources). After regenerating, re-apply any bespoke home/job/news fixtures if needed.

### Customize an API response

1. Find the API method in `app/src/main/assets/mock/api/_index.json`.
2. Open the referenced fixture.
3. Keep the `code`, `message`, and `data` envelope expected by `JcsResponse<T>`.
4. Preserve discriminators such as `module_type`, `content_type`, status values, and IDs.
5. Run the validation test above and build the app.

Local images are referenced as `android.resource://com.jiechengsheng.city/drawable/<name>` or `.../mipmap/<name>`.

### Demo images and app icon

- `app/src/main/res/drawable-nodpi/`: compressed JPEG demo photos and `mock_logo.png`.
- `app/src/main/res/drawable/ic_module_*.xml`: vector icons for the home service grid.
- `app/src/main/res/drawable/ic_launcher_foreground.xml` and `ic_launcher_background.xml`: adaptive app icon.
- `app/src/main/res/mipmap-anydpi-v26/ic_app_logo*.xml`: launcher icon entry points.

## Removed commercial components

- JPush/JCore: dependencies, manifest receivers/services/providers, and `libjcore*.so` removed.
- Mob ShareSDK / MobSDK Gradle plugin: removed; sharing uses `Intent.ACTION_SEND`, and third-party login is simulated locally.
- Facebook SDK and Facebook App Links integration: removed.
- Jiaozi/ijkplayer video player: replaced by the GSYVideoPlayer already used by hotel media pages. The remaining `libijk*.so` files are GSY's native playback engines; remove the GSY dependencies only if video playback is not needed.

## Local configuration

The repository contains no production credentials, private account details, API tokens, passwords, or contact information. Configure your own Google Maps key locally if you need live map tiles. The offline mock mode does not require a running backend.

## License and attribution

The original project and its dependencies may have separate copyright and licensing terms. This repository does not claim ownership of the legacy source code and does not add a blanket open-source license. Confirm redistribution rights and add an appropriate license only after obtaining the necessary authorization.
