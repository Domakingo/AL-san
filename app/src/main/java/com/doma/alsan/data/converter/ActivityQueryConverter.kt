package com.doma.alsan.data.converter

import com.doma.alsan.ActivityQuery
import com.doma.alsan.data.response.anilist.Activity
import com.doma.alsan.data.response.anilist.ListActivity
import com.doma.alsan.data.response.anilist.MessageActivity
import com.doma.alsan.data.response.anilist.TextActivity

fun ActivityQuery.Data.convert(): Activity {
    return when (Activity?.__typename) {
        "TextActivity" -> {
            Activity?.onTextActivity?.convert() ?: TextActivity()
        }
        "ListActivity" -> {
            Activity?.onListActivity?.convert() ?: ListActivity()
        }
        "MessageActivity" -> {
            Activity?.onMessageActivity?.convert() ?: MessageActivity()
        }
        else -> TextActivity()
    }
}