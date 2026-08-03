# WhereRebirth

WhereRebirth is an offline-restored Android local-life application based on a discontinued legacy project. It is maintained for learning, compatibility research, and portfolio demonstration.

## Offline restoration

- Retrofit API calls are intercepted locally before reaching the retired server.
- Mock responses are generated from the JavaBean return type of each API method.
- Shared mock defaults live in `app/src/main/assets/mock/defaults.json`.
- Retrofit converters, RxJava flow, `BaseObserver`, and existing `onError` handlers are preserved.
- Remote image fields are replaced by the bundled application logo.
- Google Maps SDK and map screens remain in the project.
- Google and Facebook authorization are simulated as successful for offline preview.

## Build

```bash
./gradlew :app:assembleDevDebug
```

## Local configuration

This repository intentionally contains no production credentials, private account details, API tokens, passwords, or contact information. Configure your own third-party keys locally when you need live Google Maps or other provider functionality. The offline mock mode does not require a running backend.

## License and attribution

The original project and its dependencies may have separate copyright and licensing terms. This repository does not claim ownership of the legacy source code and does not add a blanket open-source license. Confirm redistribution rights and add an appropriate license only after obtaining the necessary authorization.
