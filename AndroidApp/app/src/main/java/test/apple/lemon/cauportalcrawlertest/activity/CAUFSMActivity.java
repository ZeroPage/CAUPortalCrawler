package test.apple.lemon.cauportalcrawlertest.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import test.apple.lemon.cauportalcrawlertest.Pref;
import test.apple.lemon.cauportalcrawlertest.R;
import test.apple.lemon.cauportalcrawlertest.activity.gadget.JsWebView;
import timber.log.Timber;


public class CAUFSMActivity extends Activity {

    @InjectView(R.id.webView)
    JsWebView webView;

    @InjectView(R.id.popupView)
    JsWebView popupView;
    private State mState;

//    @OnClick(R.id.buttonBackUp)
//    void upViewBack(Button view) {
//        webView.reload();
//    }
//
//    @OnClick(R.id.buttonBackDown)
//    void downViewBack(Button view) {
//        if (popupView.canGoBack()) {
//            popupView.goBack();
//        } else {
//            popupView.clearHistory();
//            popupView.loadUrl("");
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_cau_fsm);
        ButterKnife.inject(this);

        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Timber.d("onPageFinished:%s", url);
                mState = mState.receiveURL(view, view.getUrl());
                mState.process(view);
            }
        };
        WebChromeClient webChromeClient = new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    //http://stackoverflow.com/questions/12076494/onload-in-iframes-not-working-if-iframe-have-non-html-document-in-src-pdf-or-t
                    Timber.d("onProgressChanged:%s", view.getOriginalUrl());
                    mState.checkIFrameLoaded(view);
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
                    mState = mState.resultOfJsForKey(view, key, val);
                }
                result.confirm();
                return true;
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popupView);
                resultMsg.sendToTarget();
                return true;
            }
        };
        JsWebView.OnJsResultListener jsResultListener = new JsWebView.OnJsResultListener() {
            @Override
            public void onJsResult(final WebView view, final String key, final String android_val) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        mState = mState.resultOfJsForKey(view, key, android_val);
                    }
                });
            }
        };

        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        webView.addJavascriptInterface(webView, "android");
        webView.setOnJsResultListener(jsResultListener);
        WebSettings mainSettings = webView.getSettings();
        mainSettings.setJavaScriptEnabled(true);
        mainSettings.setSupportMultipleWindows(true);

        popupView.setWebViewClient(webViewClient);
        popupView.setWebChromeClient(webChromeClient);
        popupView.addJavascriptInterface(popupView, "android");
        popupView.setOnJsResultListener(jsResultListener);
        WebSettings popupSettings = popupView.getSettings();
        popupSettings.setJavaScriptEnabled(true);

        start();
    }

    public void start() {
        Timber.d("start");
        mState = State.START;
        mState.process(webView);
    }

    public static enum State {
        UNKNOWN,
        START {
            @Override
            public State receiveURL(WebView webView, String url) {
                if (url.startsWith("http://portal.cau.ac.kr/_layouts/Cau/Member/Login.aspx")) {
                    return LOGIN;
                } else if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                    return MAIN;
                } else {
                    return UNKNOWN;
                }
            }

            @Override
            public void process(WebView webView) {
                webView.loadUrl("http://portal.cau.ac.kr");
                //        mWebView.loadUrl("http://cautis.cau.ac.kr/SMT/main.jsp"); // 모바일 페이지. 좀더 편리 할듯하나
            }
        },
        LOGIN {
            @Override
            public State receiveURL(WebView webView, String url) {
                if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                    return MAIN;
                } else {
                    return this;
                }
            }

            @Override
            public void process(WebView webView) {
                runJS(webView,
                        "not_use", "document.querySelector('#txtUserID').value='" + Pref.ID + "';" +
                                "document.querySelector('#txtUserPwd').value='" + Pref.PASSWD + "';" +
                                "document.querySelector('#btnLogin').click();");
            }
        },
        MAIN {

            private String nextUrl;

            @Override
            public State receiveURL(WebView webView, String url) {
                return url.startsWith(nextUrl) ? ECLASS : UNKNOWN;
            }

            @Override
            public void process(WebView webView) {
                nextUrl = "http://portal.cau.ac.kr/Eclass/Pages/e_class.aspx";
                webView.loadUrl(nextUrl);
            }
        },
        ECLASS {
            private String nextUrl;

            @Override
            public State receiveURL(WebView webView, String url) {
                if (url.startsWith(nextUrl)) {
                    return ECLASS_LIST;
                } else {
                    return UNKNOWN;
                }
            }

            @Override
            public void process(WebView webView) {
                //            runJS(webView, "$('#External_Content_IFrame').attr('src');"); // 만일 이클래스 강의실 페이지가 사용자마다 다른 동적 값일 경우.
                nextUrl = "http://cautis.cau.ac.kr/LMS/websquare/websquare.jsp";
                webView.loadUrl(nextUrl + "?w2xPath=/LMS/comm/main.xml");
            }
        },
        ECLASS_LIST {

            private final String contentFrame = "contentFrame";

            @Override
            public void checkIFrameLoaded(WebView webView) {
                runJS(webView, contentFrame, "document.querySelector('#contentFrame').contentDocument.querySelectorAll('#infomationCourse_body_tbody > tr > td:first-child').length;");
            }

            @Override
            public State resultOfJsForKey(WebView webView, String key, String android_val) {
                if (contentFrame.equals(key)) {
                    int i = Integer.parseInt(android_val);
                    if (i != 0) {
                        runJS(webView, "not_use", "document.querySelector('#contentFrame').contentDocument.querySelectorAll('#infomationCourse_body_tbody > tr > td:nth-child(1)')[0].click();");
                        return WAIT_FOR_BOARD;
                    }
                }
                return this;
            }
        },
        WAIT_FOR_BOARD {
            private final String contentFrame = "contentFrame";

            @Override
            public void checkIFrameLoaded(WebView webView) {
                runJS(webView, contentFrame, "document.querySelector('#menuFrame').contentDocument.querySelector('#repeat5_1_repeat6 > table > tbody > tr:nth-child(1) > td > div > div') != null");
            }

            @Override
            public State resultOfJsForKey(WebView webView, String key, String android_val) {
                if (contentFrame.equals(key)) {
                    boolean b = Boolean.parseBoolean(android_val);
                    if (b) {
                        runJS(webView, "not_use", "document.querySelector('#menuFrame').contentDocument.querySelector('#repeat5_1_repeat6 > table > tbody > tr:nth-child(1) > td > div > div').click();");
                        // $('#menuFrame').contentDocument.querySelectorAll('#repeat5_1_repeat6_0_group7 > div')[0].click();
                        // text를 가지고 있는 녀석을 찾아서 click 하면 됨.
                        return UNKNOWN;
                    }
                }
                return this;
            }
        };

        private static void runJS(WebView webView, String key, String script) {
            String js = "javascript:" +
                    "var android_var=" + script + ";" +
                    "alert('@" + key + ":'+android_var);";
            webView.loadUrl(js);
        }

        public State receiveURL(WebView webView, String url) {
            Timber.d(url);
            return this;
            //this.process(webView);
        }

        public void process(WebView webView) {

        }

        public void checkIFrameLoaded(WebView webView) {

        }

        public State resultOfJsForKey(WebView webView, String key, String android_val) {
            return this;
        }
    }
}
