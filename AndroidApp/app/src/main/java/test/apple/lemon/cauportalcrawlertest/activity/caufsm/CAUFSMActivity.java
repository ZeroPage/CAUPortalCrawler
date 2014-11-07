package test.apple.lemon.cauportalcrawlertest.activity.caufsm;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import test.apple.lemon.cauportalcrawlertest.R;
import timber.log.Timber;


public class CAUFSMActivity extends Activity {

    @InjectView(R.id.textViewForState)
    TextView textViewForState;

    @InjectView(R.id.webView)
    WebView mainWebView;

    @InjectView(R.id.popupViewLayout)
    LinearLayout popupViewLayout;

    private WebViewState state;

    private WebViewClient webViewClient;
    private WebChromeClient webChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_cau_fsm);
        ButterKnife.inject(this);

        webViewClient = new FSMWebViewClient();
        webChromeClient = new FSMWebChromeClient();

        mainWebView.setWebViewClient(webViewClient);
        mainWebView.setWebChromeClient(webChromeClient);
        WebSettings mainSettings = mainWebView.getSettings();
        mainSettings.setJavaScriptEnabled(true);
        mainSettings.setSupportMultipleWindows(true);

        popupViewLayout.removeAllViews();
        popupViewLayout.setVisibility(View.VISIBLE);

        WebViewState.setHelper(new WebViewState.StateHelper() {
            private Timer timer;
            private TimerTask task;
            private int boardIndex;

            @Override
            public void setState(final WebView webView, WebViewState changeTo) {
                updateTimeout(webView);
                state = changeTo;
                switch (state) {
                    case START:
                        initLayout();
                        break;
                    case FINAL:
                        finish();
                        break;
                }
                textViewForState.setText(state.name());
            }

            @Override
            public void initBoardIndex() {
                boardIndex = 0;
            }

            @Override
            public int getBoardIndex() {
                return boardIndex;
            }

            @Override
            public void setBoardIndex(int newIndex) {
                boardIndex = newIndex;
            }

            private void updateTimeout(final WebView webView) {
                if (task != null) {
                    task.cancel();
                }
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                timer = new Timer();
                task = new TimerTask() {
                    @Override
                    public void run() {
                        webView.post(new Runnable() {
                            @Override
                            public void run() {
                                state.onTimeout(webView);
                            }
                        });
                    }
                };
                timer.schedule(task, 15 * 1000); //15sec
            }
        });

        // todo network state check.
        WebViewState.start(mainWebView);
    }

    private void initLayout() {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        popupViewLayout.removeAllViews();
        popupViewLayout.setVisibility(View.INVISIBLE);
    }

    private class FSMWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                //http://stackoverflow.com/questions/12076494/onload-in-iframes-not-working-if-iframe-have-non-html-document-in-src-pdf-or-t
                Timber.d("onProgressChanged:%s", view.getOriginalUrl());
                state.onProgressChanged(view);
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            // for ignore any of alert dialog!
            if (message.startsWith("@")) {
                String sub = message.substring(1);
                Uri uri = Uri.parse(sub);
                String key = uri.getScheme();
                String val = uri.getSchemeSpecificPart();
                state.onJsAlert(view, key, val);
            }
            result.confirm();
            return true;
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            Context baseContext = getBaseContext();
            WebView popupView = new WebView(baseContext);
            popupView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            popupViewLayout.removeAllViews();
            popupViewLayout.setVisibility(View.VISIBLE);
            popupViewLayout.addView(popupView);

            popupView.setWebViewClient(webViewClient);
            popupView.setWebChromeClient(webChromeClient);
            WebSettings popupSettings = popupView.getSettings();
            popupSettings.setJavaScriptEnabled(true);

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(popupView);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            super.onCloseWindow(window);
            state = WebViewState.ECLASS_LIST;
            textViewForState.setText(state.name());
            initLayout();
            WebViewState.runJSPublic(mainWebView, "'close'", "close");
        }
    }

    private class FSMWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView webView, String url) {
            Timber.d("onPageFinished:%s", url);
            state.onPageFinished(webView, webView.getUrl());
        }
    }
}
