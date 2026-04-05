package com.doma.alsan.data.response.mal

import com.google.gson.annotations.SerializedName

data class EpisodeListResponse(
    @SerializedName("data")
    val data: List<EpisodeDataResponse>? = null,
    @SerializedName("pagination")
    val pagination: EpisodePaginationResponse? = null
)

data class EpisodeDataResponse(
    @SerializedName("mal_id")
    val malId: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("title_japanese")
    val titleJapanese: String? = null,
    @SerializedName("title_romanji")
    val titleRomanji: String? = null,
    @SerializedName("aired")
    val aired: String? = null,
    @SerializedName("filler")
    val filler: Boolean? = null,
    @SerializedName("recap")
    val recap: Boolean? = null,
    @SerializedName("url")
    val url: String? = null
)

data class EpisodePaginationResponse(
    @SerializedName("last_visible_page")
    val lastVisiblePage: Int? = null,
    @SerializedName("has_next_page")
    val hasNextPage: Boolean? = null
)
