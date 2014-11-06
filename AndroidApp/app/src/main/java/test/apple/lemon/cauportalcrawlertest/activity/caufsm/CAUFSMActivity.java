package test.apple.lemon.cauportalcrawlertest.activity.caufsm;

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
import test.apple.lemon.cauportalcrawlertest.activity.gadget.JsWebView;
import timber.log.Timber;


public class CAUFSMActivity extends Activity {

    @InjectView(R.id.textViewForState)
    TextView textViewForState;

    @InjectView(R.id.webView)
    JsWebView mainWebView;

    @InjectView(R.id.popupViewLayout)
    LinearLayout popupViewLayout;

    private WebViewState state;

    private JsWebView.OnTimeoutListener onTimeoutListener = new JsWebView.OnTimeoutListener() {
        @Override
        public void onTimeout(WebView webView) {
            state.onTimeout(webView);
            textViewForState.setText(state.name());
            initLayout();
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

        WebViewState.setStateListener(new WebViewState.StateListener() {
            @Override
            public void onStartState() {
                textViewForState.setText(state.name());
                initLayout();
            }

            @Override
            public void onFinalState() {
                finish();
            }

            @Override
            public void onStateChange(WebViewState changeTo) {
                state = changeTo;
            }
        });


        // todo network state check.
        WebViewState.start(mainWebView);
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
                textViewForState.setText(state.name());
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
            state = WebViewState.ECLASS_LIST;
            textViewForState.setText(state.name());
            initLayout();
            WebViewState.runJSPublic(mainWebView, "'close'", "close");
        }
    }

    private void initLayout() {
        popupViewLayout.removeAllViews();
        popupViewLayout.setVisibility(View.INVISIBLE);
    }

    private class FSMWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView webView, String url) {
            Timber.d("onPageFinished:%s", url);
            //state =
            state.onPageFinished(webView, webView.getUrl());
            textViewForState.setText(state.name());
        }
    }
}
