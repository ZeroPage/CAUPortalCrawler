package test.apple.lemon.cauportalcrawlertest.fsm;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 9. 25..
 */
public class SearchingStateManager {
    final WebView popupView;
    private SearchingState state;

    public SearchingStateManager(WebView popupView) {
        this.popupView = popupView;
        this.popupView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Timber.d("onPageFinished");
                state = state.receiveURL(view.getUrl());
            }
        });
        this.popupView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) { // java script done 캐치 가능.
                if (newProgress == 100) {
                    Timber.d("onProgressChanged");
                }
            }
        });
        WebSettings popupSettings = this.popupView.getSettings();
        popupSettings.setJavaScriptEnabled(true);
    }

    public void initToStartState() {
        state = SearchingState.START;
    }
}
