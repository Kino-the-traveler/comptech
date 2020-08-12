package org.compact.tech

import android.content.Context

class ScrPreferences(context: Context) {
    private val name = "ScrPreferences"
    private val prId = "prId"
    private val cookies = "cookies"
    private val link = "link"

    private val preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    fun savePreland(prId: String) = preferences.edit().putString(this.prId, prId).apply()
    fun getPreland() = preferences.getString(this.prId, null)

    fun saveCookies(cookies: String) = preferences.edit().putString(this.cookies, cookies).apply()
    fun getCookies() = preferences.getString(this.cookies, null)

    fun saveLink(link: String) = preferences.edit().putString(this.link, link).apply()
    fun getLink() = preferences.getString(this.link, null)


}