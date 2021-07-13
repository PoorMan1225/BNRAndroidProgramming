package com.rjhwork.mycompany.photogallery

import android.content.Context

private const val PREF_SEARCH_QUERY = "searchQuery"
private const val PREF_SEARCH = "search"

object QueryPreferences {

    fun getStoredQuery(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_SEARCH, Context.MODE_PRIVATE)
        return prefs.getString(PREF_SEARCH_QUERY, "")!!
    }

    fun setStoredQuery(context:Context, query:String) {
        context.getSharedPreferences(PREF_SEARCH, Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_SEARCH_QUERY, query)
            .apply()
    }
}