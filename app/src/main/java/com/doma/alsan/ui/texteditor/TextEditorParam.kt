package com.doma.alsan.ui.texteditor

import com.doma.alsan.helper.enums.TextEditorType

data class TextEditorParam(
    val textEditorType: TextEditorType,
    val activityId: Int? = null,
    val activityReplyId: Int? = null,
    val recipientId: Int? = null,
    val username: String? = null
)
