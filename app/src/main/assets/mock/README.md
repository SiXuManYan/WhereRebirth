# Local API mock data

For complete English and Chinese maintenance instructions, see the repository root
`README.md` and `README.zh-CN.md`. Keep both documents synchronized when the mock workflow changes.

WhereRebirth serves Retrofit requests locally because the legacy server is no longer available.
`LocalMockInterceptor` selects `api/<Retrofit method key>.json`; missing or type-incompatible files
fall back to JSON generated from the JavaBean return type. Lists and page data contain five records
by default.

Run `gradlew :app:testDevDebugUnitTest --tests *.GenerateApiMocksTest` to regenerate every fixture
after editing `RetrofitApi` or its response JavaBeans. `_index.json` maps API methods to files.
Regeneration overwrites hand-edited fixtures, including the home page data.

Run `gradlew :app:testDevDebugUnitTest --tests *.ValidateApiMocksTest` to verify that every fixture
parses as the declared `JcsResponse<T>` type.

The existing Retrofit converters, RxJava observers, and `onError` handlers are intentionally kept.
To test an error flow, temporarily change the generated response code in the interceptor from `200`
to the desired API code (for example `404` or `500`).

Images use bundled local resources such as
`android.resource://com.jiechengsheng.city/drawable/mock_banner_1` or the vector
`drawable/ic_module_*` icons. See `app/src/main/res/drawable-nodpi/`.
