package test.apple.lemon.cauportalcrawlertest.fsm;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by rino0601 on 2014. 9. 25..
 */
public class SearchingStateManager {
    final WebView popupView;
    private SearchingState state;

    public SearchingStateManager(WebView popupView) {
        this.popupView = popupView;
        this.popupView.setWebViewClient(new WebViewClient());
        this.popupView.setWebChromeClient(new ChromeClient());
        WebSettings popupSettings = this.popupView.getSettings();
        popupSettings.setJavaScriptEnabled(true);
    }

    public void initToStartState() {
        state = SearchingState.START;
    }

    private class ChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) { // java script done 캐치 가능.
            if (newProgress == 100)
                state = state.receiveURL(view.getUrl());
        }

    }

}
