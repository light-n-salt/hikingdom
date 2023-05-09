package com.example.hikingdom.ui.main.home

import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import com.example.hikingdom.ApplicationClass.Companion.HOME_WEB_URL
import com.example.hikingdom.databinding.FragmentHomeBinding
import com.example.hikingdom.ui.BaseFragment

class HomeFragment(): BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    override fun initAfterBinding() {
        webViewSetting()
    }

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
    }

    fun webViewSetting(){
        val webView = binding.homeWebview
        webView.webViewClient = WebViewClient()
        webView.loadUrl(HOME_WEB_URL)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    System.exit(0)
                }
            }
        })
    }
}