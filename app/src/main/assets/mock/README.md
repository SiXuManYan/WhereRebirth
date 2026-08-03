# Local API mock data

WhereRebirth serves Retrofit requests locally because the legacy server is no longer available.
`LocalMockInterceptor` builds successful JSON from the JavaBean return type of each API method.

The existing Retrofit converters, RxJava observers, and `onError` handlers are intentionally kept.
To test an error flow, temporarily change the generated response code in the interceptor from `200`
to the desired API code (for example `404` or `500`).

Remote image URLs are replaced by the bundled `ic_app_logo` resource.
