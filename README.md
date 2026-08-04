# WhereRebirth

[简体中文](README.zh-CN.md) | English

WhereRebirth is an offline-restored Android local-life application based on a discontinued legacy project. It is maintained for learning, compatibility research, and portfolio demonstration.

## Offline restoration

- Retrofit API calls are intercepted locally before reaching the retired server.
- Each `RetrofitApi` method has an editable JSON fixture under `app/src/main/assets/mock/api/`.
- `LocalMockInterceptor` uses the Retrofit method signature to select the matching fixture.
- Missing fixtures fall back to JSON generated from the declared JavaBean response type.
- Lists and `PageResponse<T>` payloads contain five generated records by default.
- Retrofit converters, RxJava flow, `BaseObserver`, and existing `onError` handlers are preserved.
- Remote image fields are replaced by the bundled application logo.
- Google Maps SDK and map screens remain in the project.
- Google and Facebook authorization are simulated as successful for offline preview.

## Build

```bash
./gradlew :app:assembleDevDebug
```

The project uses Gradle 7.5 and must run with a compatible JDK. JDK 17 is the recommended local Gradle JVM.

## Local API mock data

The legacy backend is no longer available, so API requests are satisfied locally while retaining the existing Retrofit and RxJava call chain.

### Files

- `app/src/main/assets/mock/api/`: one response fixture per `RetrofitApi` method.
- `app/src/main/assets/mock/api/_index.json`: mapping between Retrofit method names, fixture filenames, and return types.
- `app/src/main/java/com/jiechengsheng/city/api/LocalMockInterceptor.kt`: loads fixture files and creates HTTP responses.
- `app/src/main/java/com/jiechengsheng/city/api/MockJsonFactory.kt`: generates fallback JSON from JavaBean types.
- `app/src/test/java/com/jiechengsheng/city/api/GenerateApiMocksTest.kt`: regenerates and validates every fixture.

Overloaded Retrofit methods use a parameter-type suffix in the filename to prevent collisions. At runtime, a missing file does not trigger a real network request; the response factory generates a compatible fallback instead.

### Regenerate all fixtures

Run this command after adding an API method or changing a response JavaBean:

```bash
./gradlew :app:testDevDebugUnitTest --tests "com.jiechengsheng.city.api.GenerateApiMocksTest" --rerun-tasks
```

On Windows PowerShell:

```powershell
.\gradlew.bat :app:testDevDebugUnitTest --tests "com.jiechengsheng.city.api.GenerateApiMocksTest" --rerun-tasks
```

The generator writes complete `JcsResponse<T>` payloads, creates approximately five records for collection and pagination responses, and validates that every generated file contains valid JSON.

### Customize an API response

1. Find the API method in `app/src/main/assets/mock/api/_index.json`.
2. Open the referenced fixture.
3. Keep the `code`, `message`, and `data` response envelope expected by `JcsResponse<T>`.
4. Preserve business-critical discriminator fields such as `module_type`, item types, status values, and IDs.
5. Build and run the app; no backend server is required.

The generated data is intended as a safe starting point. Screens with complex business rules may still require manually adjusted values. To test an error flow, change the selected fixture's `code` and `message` or return the desired HTTP status from the interceptor.

## Local configuration

This repository intentionally contains no production credentials, private account details, API tokens, passwords, or contact information. Configure your own third-party keys locally when you need live Google Maps or other provider functionality. The offline mock mode does not require a running backend.

## License and attribution

The original project and its dependencies may have separate copyright and licensing terms. This repository does not claim ownership of the legacy source code and does not add a blanket open-source license. Confirm redistribution rights and add an appropriate license only after obtaining the necessary authorization.
