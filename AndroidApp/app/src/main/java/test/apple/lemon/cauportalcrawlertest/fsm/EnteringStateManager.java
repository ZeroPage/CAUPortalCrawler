package test.apple.lemon.cauportalcrawlertest.fsm;

import android.os.Message;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 9. 25..
 */
public class EnteringStateManager {
    private final SearchingStateManager mNext;
    private final WebView mWebView;
    private EnteringState mState;

    public EnteringStateManager(WebView webView, SearchingStateManager nextStateManager) {
        mNext = nextStateManager;
        mWebView = webView;
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Timber.d("onPageFinished");
                mState = mState.receiveURL(view.getUrl());
                mState.doAction(mWebView);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    Timber.d("onProgressChanged");
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                mNext.initToStartState();
                transport.setWebView(mNext.popupView);
                resultMsg.sendToTarget();
                return true;
            }
        });
        WebSettings mainSettings = mWebView.getSettings();
        mainSettings.setJavaScriptEnabled(true);
        mainSettings.setSupportMultipleWindows(true);
    }

    public void start() {
        Timber.d("start");
        mState = EnteringState.START;
        mWebView.loadUrl("http://portal.cau.ac.kr");
//        mWebView.loadUrl("http://cautis.cau.ac.kr/SMT/main.jsp"); // 모바일 페이지. 좀더 편리 할듯하나
    }
}
