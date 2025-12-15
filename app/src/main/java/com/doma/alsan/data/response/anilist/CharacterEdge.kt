package com.doma.alsan.data.response.anilist

import com.doma.alsan.type.CharacterRole


data class CharacterEdge(
    val node: Character = Character(),
    val role: CharacterRole? = null,
    val name: String = "",
    val voiceActorRoles: List<StaffRoleType> = listOf(),
    val media: List<Media> = listOf()
)