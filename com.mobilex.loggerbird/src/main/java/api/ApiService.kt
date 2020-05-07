package api

interface ApiService {

    fun login(
        email: String?,
        password: String?,
        callback: JsonResponseCallback?
    )

    fun exchangeAuthCodeForToken(
        code: String?,
        callback: JsonResponseCallback?
    )

    fun addIssue(
        title: String?,
        content: String?,
        priority: String?,
        kind: String?,
        callback: StringResponseCallback?
    )

    fun refreshToken(
        refreshToken: String?,
        callback: JsonResponseCallback?
    )
}