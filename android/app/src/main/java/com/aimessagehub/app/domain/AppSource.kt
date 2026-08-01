package com.aimessagehub.app.domain

enum class AppSource(
    val packageName: String,
    val displayName: String,
) {
    WECHAT("com.tencent.mm", "微信"),
    QQ("com.tencent.mobileqq", "QQ"),
    SOUL("cn.soulapp.android", "Soul"),
    XIAOHONGSHU("com.xingin.xhs", "小红书"),
    GENERIC("", "其他 App");

    companion object {
        fun fromPackageName(packageName: String?): AppSource =
            entries.firstOrNull { it.packageName == packageName } ?: GENERIC
    }
}

