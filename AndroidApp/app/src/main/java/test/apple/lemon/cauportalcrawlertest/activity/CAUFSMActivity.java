package test.apple.lemon.cauportalcrawlertest.activity;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import test.apple.lemon.cauportalcrawlertest.R;
import test.apple.lemon.cauportalcrawlertest.State;
import test.apple.lemon.cauportalcrawlertest.activity.gadget.JsWebView;
import timber.log.Timber;


public class CAUFSMActivity extends Activity {

    @InjectView(R.id.textViewForState)
    TextView textViewForState;

    @InjectView(R.id.webView)
    JsWebView mainWebView;

    @InjectView(R.id.popupViewLayout)
    LinearLayout popupViewLayout;

    private State mState;
    private JsWebView.OnTimeoutListener onTimeoutListener = new JsWebView.OnTimeoutListener() {
        @Override
        public void onTimeout(WebView webView) {
            // todo, sub-webview 고칠 것.
            mState = mState.onTimeout(webView);
            mState.invoke(webView);
            textViewForState.setText(mState.name());
        }
    };
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
        mainWebView.setOnTimeoutListener(onTimeoutListener);
        WebSettings mainSettings = mainWebView.getSettings();
        mainSettings.setJavaScriptEnabled(true);
        mainSettings.setSupportMultipleWindows(true);

        popupViewLayout.removeAllViews();
        popupViewLayout.setVisibility(View.VISIBLE);

        State.setStateListener(new State.StateListener() {
            @Override
            public void onFinalState() {
                finish();
            }
        });

        // todo network state check.
        start();
    }

    public void start() {
        mState = State.START;
        mState.invoke(mainWebView);
        textViewForState.setText(mState.name());
    }

    private class FSMWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                //http://stackoverflow.com/questions/12076494/onload-in-iframes-not-working-if-iframe-have-non-html-document-in-src-pdf-or-t
                Timber.d("onProgressChanged:%s", view.getOriginalUrl());
                mState.onProgressChanged(view);
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
                mState = mState.onJsAlert(view, key, val);
                textViewForState.setText(mState.name());
            }
            result.confirm();
            return true;
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            Context baseContext = getBaseContext();
            JsWebView popupView = new JsWebView(baseContext);
            popupView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            popupViewLayout.removeAllViews();
            popupViewLayout.setVisibility(View.VISIBLE);
            popupViewLayout.addView(popupView);

            popupView.setWebViewClient(webViewClient);
            popupView.setWebChromeClient(webChromeClient);
            popupView.setOnTimeoutListener(onTimeoutListener);
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
            popupViewLayout.removeAllViews();
            popupViewLayout.setVisibility(View.INVISIBLE);
            mState = State.ECLASS_LIST;
            textViewForState.setText(mState.name());
            State.runJSPublic(mainWebView, "'close'", "close");
        }
    }

    private class FSMWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView webView, String url) {
            Timber.d("onPageFinished:%s", url);
            mState = mState.onPageFinished(webView, webView.getUrl());
            mState.invoke(webView);
            textViewForState.setText(mState.name());
        }
    }
}
