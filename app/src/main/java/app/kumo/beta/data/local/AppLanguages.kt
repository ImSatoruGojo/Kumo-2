package app.kumo.beta.data.local

data class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
)

object AppLanguages {
    val supportedLanguages = listOf(
        AppLanguage("en", "English", "English"),
        AppLanguage("es", "Spanish", "Español"),
        AppLanguage("fr", "French", "Français"),
        AppLanguage("de", "German", "Deutsch"),
        AppLanguage("it", "Italian", "Italiano"),
        AppLanguage("pt", "Portuguese", "Português"),
        AppLanguage("pt-BR", "Brazilian Portuguese", "Português (Brasil)"),
        AppLanguage("ru", "Russian", "Русский"),
        AppLanguage("ja", "Japanese", "日本語"),
        AppLanguage("ko", "Korean", "한국어"),
        AppLanguage("zh-CN", "Simplified Chinese", "中文(简体)"),
        AppLanguage("zh-TW", "Traditional Chinese", "繁體中文"),
        AppLanguage("ar", "Arabic", "العربية"),
        AppLanguage("hi", "Hindi", "हिन्दी"),
        AppLanguage("th", "Thai", "ไทย"),
        AppLanguage("vi", "Vietnamese", "Tiếng Việt"),
        AppLanguage("id", "Indonesian", "Bahasa Indonesia"),
        AppLanguage("pl", "Polish", "Polski"),
        AppLanguage("tr", "Turkish", "Türkçe"),
        AppLanguage("nl", "Dutch", "Nederlands"),
        AppLanguage("sv", "Swedish", "Svenska"),
        AppLanguage("da", "Danish", "Dansk"),
        AppLanguage("fi", "Finnish", "Suomi"),
        AppLanguage("no", "Norwegian", "Norsk"),
        AppLanguage("cs", "Czech", "Čeština"),
        AppLanguage("hu", "Hungarian", "Magyar"),
        AppLanguage("ro", "Romanian", "Română"),
        AppLanguage("el", "Greek", "Ελληνικά"),
        AppLanguage("uk", "Ukrainian", "Українська"),
        AppLanguage("bg", "Bulgarian", "Български"),
        AppLanguage("hr", "Croatian", "Hrvatski"),
        AppLanguage("sk", "Slovak", "Slovenčina"),
        AppLanguage("et", "Estonian", "Eesti"),
        AppLanguage("lv", "Latvian", "Latviešu"),
        AppLanguage("lt", "Lithuanian", "Lietuvių"),
        AppLanguage("he", "Hebrew", "עברית"),
        AppLanguage("fa", "Persian", "فارسی"),
        AppLanguage("mk", "Macedonian", "Македонски"),
        AppLanguage("ml", "Malayalam", "മലയാളം"),
        AppLanguage("ta", "Tamil", "தமிழ்"),
        AppLanguage("te", "Telugu", "తెలుగు")
    )

    fun getLanguageByCode(code: String): AppLanguage {
        return supportedLanguages.find { it.code.equals(code, ignoreCase = true) }
            ?: supportedLanguages.first()
    }
}
