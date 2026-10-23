package com.jiechengsheng.city.utils

/** 本地演示模式下的第三方账号信息，替代 ShareSDK 的 PlatformDb。 */
data class ThreePartyAccount(
    val platformName: String,
    val userName: String,
    val userId: String,
    val userIcon: String,
)
