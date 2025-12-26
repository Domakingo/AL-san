package com.doma.alsan.data.converter

import com.doma.alsan.ActivityQuery
import com.doma.alsan.data.response.anilist.Activity
import com.doma.alsan.data.response.anilist.ListActivity
import com.doma.alsan.data.response.anilist.MessageActivity
import com.doma.alsan.data.response.anilist.TextActivity

fun ActivityQuery.Data.convert(): Activity {
    val activity = Activity ?: return TextActivity()
    return when (activity.__typename) {
        "TextActivity" -> {
            activity.onTextActivity?.convert() ?: TextActivity()
        }
        "ListActivity" -> {
            activity.onListActivity?.convert() ?: ListActivity()
        }
        "MessageActivity" -> {
            activity.onMessageActivity?.convert() ?: MessageActivity()
        }
        else -> TextActivity()
    }
}