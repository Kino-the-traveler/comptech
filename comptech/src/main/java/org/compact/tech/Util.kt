package org.compact.tech

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView

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
