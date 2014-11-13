package test.apple.lemon.cauportalcrawlertest.activity.caufsm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import test.apple.lemon.cauportalcrawlertest.R;
import test.apple.lemon.cauportalcrawlertest.model.EClassContent;
import test.apple.lemon.cauportalcrawlertest.model.LocalProperties;
import test.apple.lemon.cauportalcrawlertest.model.helper.PrefHelper;
import timber.log.Timber;


public class CAUWebActivity extends Activity {

    private static final String KEY_LECTURE_INDEX = "lectureIndex";
    private static final String KEY_BOARD_INDEX = "boardIndex";
    private static final String KEY_ITEM_INDEX = "itemIndex";
    @InjectView(R.id.theLayout)
    RelativeLayout theLayout;
    @InjectView(R.id.textViewForState)
    TextView textViewForState;
    @InjectView(R.id.webView)
    WebView mainWebView;
    @InjectView(R.id.popupViewLayout)
    LinearLayout popupViewLayout;
    private WebViewState state;
    private WebViewClient webViewClient;
    private WebChromeClient webChromeClient;
    private LocalProperties localProperties;
    private int lectureIndex;
    private int boardIndex;
    private int itemIndex;
    private AlertDialog dialog;

    public static void start(Context context, int lectureIndex, int boardIndex, int itemIndex) {
        Intent intent = new Intent(context, CAUWebActivity.class);
        intent.putExtra(KEY_LECTURE_INDEX, lectureIndex);
        intent.putExtra(KEY_BOARD_INDEX, boardIndex);
        intent.putExtra(KEY_ITEM_INDEX, itemIndex);
        context.startActivity(intent);
    }

    private void initFromIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        assert extras != null;
        lectureIndex = extras.getInt(KEY_LECTURE_INDEX);
        boardIndex = extras.getInt(KEY_BOARD_INDEX);
        itemIndex = extras.getInt(KEY_ITEM_INDEX);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_cau_fsm);
        ButterKnife.inject(this);
        initFromIntent();

        theLayout.setVisibility(View.VISIBLE);
        webViewClient = new FSMWebViewClient();
        webChromeClient = new FSMWebChromeClient();
        mainWebView.setWebViewClient(webViewClient);
        mainWebView.setWebChromeClient(webChromeClient);
        WebSettings mainSettings = mainWebView.getSettings();
        mainSettings.setJavaScriptEnabled(true);
        mainSettings.setSupportMultipleWindows(true);
        popupViewLayout.removeAllViews();
        popupViewLayout.setVisibility(View.VISIBLE);

        localProperties = PrefHelper.getInstance(getBaseContext()).getPrefDao().loadData();
        WebViewState.setHelper(new WebViewState.StateHelper() {
            public int lectureMax;
            private int deltaForLecture = 0;

            @Override
            public void setState(final WebView webView, WebViewState changeTo) {
                updateTimeout(webView);
                state = changeTo;
                switch (state) {
                    case START:
                        initLayout();
                        break;
                    case FINAL:
                        dialog.dismiss();
                        break;
                }
                textViewForState.setText(state.name());
            }

            @Override
            public void initBoardIndex() {
                // do nothing.
            }

            @Override
            public int getBoardIndex() {
                return boardIndex;
            }

            @Override
            public int getBoardIndexNext() {
                return boardIndex;
            }

            @Override
            public int getLectureIndexNext() {
                return ++deltaForLecture + lectureIndex;
            }

            @Override
            public void init() {
                initFromIntent();
                deltaForLecture = 0;
            }

            @Override
            public void initLectureIndex() {
                // do nothing.
            }

            @Override
            public int getLectureIndex() {
                return lectureIndex;
            }


            @Override
            public int getLectureMax() {
                return lectureMax;
            }

            @Override
            public void setLectureMax(int numberOfLecture) {
                lectureMax = numberOfLecture;
            }

            @Override
            public boolean storeResult(List<EClassContent> contents) {
                throw new IllegalStateException("something wrong!");
            }

            @Override
            public String getPortalId() {
                return localProperties.getPortalId();
            }

            @Override
            public String getPassword() {
                return localProperties.getPassword();
            }

            @Override
            public boolean isAllowedBoard(int boardIndex) {
                return true;
            }

            @Override
            public boolean isHaveToCrawl() {
                return false;
            }

            @Override
            public int getItemIndex() {
                return itemIndex;
            }

            private void updateTimeout(final WebView webView) {
                //do nothing.
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Base_Theme_AppCompat_Light));
        dialog = builder
                .setMessage("E-Class를 읽는 중입니다. 잠시만 기다려주세요.")
                .setCancelable(false)
                .setPositiveButton("중단하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).create();
        dialog.setTitle(R.string.app_name);
        dialog.setIcon(R.drawable.ic_launcher);
        dialog.show();

        // todo network state check.
        WebViewState.start(mainWebView);
    }

    private void initLayout() {
        // todo, 여기서 state index들 다시 초기화 해줘야지;
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

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return super.onConsoleMessage(consoleMessage);
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
