package com.simplecityapps.provider.jellyfin

import com.simplecityapps.networking.retrofit.NetworkResult
import com.simplecityapps.networking.retrofit.error.HttpStatusCode
import com.simplecityapps.networking.retrofit.error.RemoteServiceHttpError
import com.simplecityapps.provider.jellyfin.http.*
import timber.log.Timber
import java.util.*

class JellyfinAuthenticationManager(
    private val userService: UserService,
    private val credentialStore: CredentialStore
) {

    private val deviceId = UUID.randomUUID().toString()

    fun getLoginCredentials(): LoginCredentials? {
        return credentialStore.loginCredentials
    }

    fun setLoginCredentials(loginCredentials: LoginCredentials?) {
        credentialStore.loginCredentials = loginCredentials
    }

    fun getAuthenticatedCredentials(): AuthenticatedCredentials? {
        return credentialStore.authenticatedCredentials
    }

    fun setAddress(host: String, port: Int) {
        credentialStore.host = host
        credentialStore.port = port
    }

    fun getHost(): String? {
        return credentialStore.host
    }

    fun getPort(): Int? {
        return credentialStore.port
    }

    fun getAddress(): String? {
        return getHost()?.let { host ->
            getPort()?.toString()?.let { port ->
                "$host:$port"
            }
        }
    }

    suspend fun authenticate(address: String, loginCredentials: LoginCredentials): Result<AuthenticatedCredentials> {
        Timber.d("authenticate(address: $address)")
        val authenticationResult = userService.authenticate(
            url = address,
            username = loginCredentials.username,
            password = loginCredentials.password,
            deviceId = deviceId
        )

        return when (authenticationResult) {
            is NetworkResult.Success<AuthenticationResult> -> {
                val authenticatedCredentials = AuthenticatedCredentials(authenticationResult.body.accessToken, authenticationResult.body.user.id)
                credentialStore.authenticatedCredentials = authenticatedCredentials
                Result.success(authenticatedCredentials)
            }
            is NetworkResult.Failure -> {
                (authenticationResult.error as? RemoteServiceHttpError)?.let { error ->
                    if (error.httpStatusCode == HttpStatusCode.Unauthorized) {
                        credentialStore.authenticatedCredentials = null
                    }
                }
                Result.failure(authenticationResult.error)
            }
        }
    }

    fun buildJellyfinPath(itemId: String, authenticatedCredentials: AuthenticatedCredentials): String? {

        if (credentialStore.host == null || credentialStore.port == null) {
            Timber.w("Invalid jellyfin address")
            return null
        }

        return "${credentialStore.host}:${credentialStore.port}" +
                "/Audio/$itemId" +
                "/universal" +
                "?UserId=${authenticatedCredentials.userId}" +
                "&DeviceId=$deviceId" +
                "&PlaySessionId=${UUID.randomUUID()}" +
                "&MaxStreamingBitrate=140000000" +
                "&Container=opus,mp3|mp3,aac,m4a,m4b|aac,flac,webma,webm,wav,ogg" +
                "&TranscodingContainer=ts" +
                "&TranscodingProtocol=hls" +
                "&MaxSampleRate=48000" +
                "&EnableRedirection=true" +
                "&EnableRemoteMedia=true" +
                "&AudioCodec=aac" +
                "&api_key=${authenticatedCredentials.accessToken}"
    }
}

/*

     val uri = "${apiClient.serverAddress}/Audio/${item.id}/universal?" +
                "UserId=${apiClient.currentUserId}&" +
                "DeviceId=${URLEncoder.encode(apiClient.deviceId, Charsets.UTF_8.name())}&" +
                "MaxStreamingBitrate=140000000&" +
                "Container=opus,mp3|mp3,aac,m4a,m4b|aac,flac,webma,webm,wav,ogg&" +
                "TranscodingContainer=ts&" +
                "TranscodingProtocol=hls&" +
                "AudioCodec=aac&" +
                "api_key=${apiClient.accessToken}&" +
                "PlaySessionId=${UUID.randomUUID()}&" +
                "EnableRemoteMedia=true"

 */