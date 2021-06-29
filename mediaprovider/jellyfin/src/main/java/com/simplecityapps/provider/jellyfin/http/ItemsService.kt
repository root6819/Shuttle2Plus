package com.simplecityapps.provider.jellyfin.http

import com.simplecityapps.networking.retrofit.NetworkResult
import retrofit2.http.*

interface ItemsService {

    @GET
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    suspend fun itemsImpl(
        @Url url: String,
        @Header("X-Emby-Token") token: String,
        @Query("recursive") recursive: Boolean = true,
        @Query("includeItemTypes") itemTypes: String = "Audio",
        @Query("fields") fields: String = "Genres",
        @Query("limit") limit: Int = 2500,
        @Query("index") index: Int = 0
    ): NetworkResult<QueryResult>

    @GET
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    suspend fun itemImpl(
        @Url url: String,
        @Header("X-Emby-Token") token: String
    ): NetworkResult<Item>
}

suspend fun ItemsService.items(
    url: String,
    token: String,
    userId: String,
    recursive: Boolean = true,
    itemTypes: String = "Audio",
    fields: String = "Genres",
    limit: Int = 2500,
    index: Int = 0
): NetworkResult<QueryResult> {
    return itemsImpl("$url/Users/$userId/Items", token, recursive, itemTypes, fields, limit, index)
}

suspend fun ItemsService.item(
    url: String,
    token: String,
    userId: String,
    itemId: String
): NetworkResult<Item> {
    return itemImpl("$url/Users/$userId/Items/$itemId", token)
}