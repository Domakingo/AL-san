package com.doma.alsan.data.response.anilist

import com.doma.alsan.helper.extensions.convertFromSnakeCase
import com.doma.alsan.type.CharacterRole
import com.doma.alsan.type.MediaRelation

data class MediaEdge(
    val node: Media = Media(),
    val relationType: MediaRelation? = null,
    val characters: List<Character> = listOf(),
    val characterRole: CharacterRole? = null,
    val characterName: String = "",
    val roleNotes: String = "",
    val dubGroup: String = "",
    val voiceActorRoles: List<StaffRoleType> = listOf(),
    val staffRole: String = "",
    val isMainStudio: Boolean = false
) {
    fun getRelationTypeString(): String {
        return relationType?.name?.convertFromSnakeCase(true) ?: ""
    }

    fun getCharacterRoleString(): String {
        return characterRole?.name?.convertFromSnakeCase(true) ?: ""
    }
}