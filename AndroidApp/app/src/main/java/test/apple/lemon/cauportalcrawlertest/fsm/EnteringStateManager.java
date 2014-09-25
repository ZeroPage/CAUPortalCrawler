package test.apple.lemon.cauportalcrawlertest.fsm;

import android.os.Message;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 9. 25..
 */
public class EnteringStateManager {
    private final SearchingStateManager next;
    private final WebView webView;
    private EnteringState state;

    public EnteringStateManager(WebView webView, SearchingStateManager next) {
        this.next = next;
        this.webView = webView;
        this.webView.setWebChromeClient(new ChromeClient());
        this.webView.setWebViewClient(new WebViewClient());
        WebSettings mainSettings = this.webView.getSettings();
        mainSettings.setJavaScriptEnabled(true);
        mainSettings.setSupportMultipleWindows(true);
    }

    public void start() {
        Timber.d("start");
        state = EnteringState.START;
        webView.loadUrl("http://portal.cau.ac.kr");
    }

    private class ChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100)
                state = state.receiveURL(view.getUrl());
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            next.initToStartState();
            transport.setWebView(next.popupView);
            resultMsg.sendToTarget();
            return true;
        }
    }
}
