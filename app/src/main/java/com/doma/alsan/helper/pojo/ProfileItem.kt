package com.doma.alsan.helper.pojo

import com.doma.alsan.data.response.anilist.*


data class ProfileItem(
    val bio: String? = null,
    var affinity: Pair<Affinity?, Affinity?>? = null,
    val tendency: Pair<Tendency?, Tendency?>? = null,
    val favoriteMedia: List<Media>? = null,
    val favoriteCharacters: List<Character>? = null,
    val favoriteStaff: List<Staff>? = null,
    val favoriteStudios: List<Studio>? = null,
    val animeStats: UserStatistics? = null,
    val mangaStats: UserStatistics? = null,
    val headerData: ProfileHeaderData? = null,
    val viewType: Int = 0
) {
    companion object {
        const val VIEW_TYPE_HEADER = 50
        const val VIEW_TYPE_BIO = 100
        const val VIEW_TYPE_AFFINITY = 200
        const val VIEW_TYPE_TENDENCY = 300
        const val VIEW_TYPE_FAVORITE_ANIME = 400
        const val VIEW_TYPE_FAVORITE_MANGA = 401
        const val VIEW_TYPE_FAVORITE_CHARACTER = 402
        const val VIEW_TYPE_FAVORITE_STAFF = 403
        const val VIEW_TYPE_FAVORITE_STUDIO = 404
        const val VIEw_TYPE_STATS = 500
        const val VIEW_TYPE_REVIEW = 600
    }
}

data class ProfileHeaderData(
    val userId: Int = 0,
    val username: String = "",
    val avatarUrl: String? = null,
    val bannerUrl: String? = null,
    val isCircleAvatar: Boolean = true,
    val animeCount: Int = 0,
    val mangaCount: Int = 0,
    val followingCount: Int = 0,
    val followersCount: Int = 0,
    val isFollowing: Boolean = false,
    val isFollower: Boolean = false,
    val isModerator: Boolean = false,
    val modRole: String? = null,
    val donatorTier: Int = 0,
    val donatorBadge: String? = null,
    val isViewer: Boolean = false
)