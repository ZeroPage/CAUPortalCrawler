package test.apple.lemon.cauportalcrawlertest.activity.caufsm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
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
import android.widget.TextView;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import test.apple.lemon.cauportalcrawlertest.AppDelegate;
import test.apple.lemon.cauportalcrawlertest.R;
import test.apple.lemon.cauportalcrawlertest.model.EClassContent;
import test.apple.lemon.cauportalcrawlertest.model.LocalProperties;
import test.apple.lemon.cauportalcrawlertest.model.helper.OrmLiteHelper;
import test.apple.lemon.cauportalcrawlertest.model.helper.PrefHelper;
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
    private LocalProperties localProperties;

    public static void start(Context context) {
        Intent intent = new Intent(context, CAUFSMActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

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

        localProperties = PrefHelper.getInstance(getBaseContext()).getPrefDao().loadData();
        WebViewState.setHelper(new WebViewState.StateHelper() {
            public int lectureMax;
            private Timer timer;
            private TimerTask task;
            private int boardIndex;
            private int lectureIndex;

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

            @Override
            public void initLectureIndex() {
                lectureIndex = 0;
            }

            @Override
            public int getLectureIndex() {
                return lectureIndex;
            }

            @Override
            public void setLectureIndex(int newIndex) {
                lectureIndex = newIndex;
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
                // todo test.
                OrmLiteHelper helper = AppDelegate.getHelper(getApplicationContext());
                RuntimeExceptionDao<EClassContent, Integer> contentsDAO = helper.getContentsDAO();
                Integer minIndex = Integer.MAX_VALUE;
                for (EClassContent content : contents) {
                    int lecture = content.getLecture();
                    int board = content.getBoard();
                    int itemIndex = content.getIndex();
                    Map<String, Object> queryMap = EClassContent.queryMap(lecture, board, itemIndex);
                    List<EClassContent> storedItem = contentsDAO.queryForFieldValues(queryMap);
                    if (storedItem.isEmpty()) {
                        contentsDAO.create(content);
                    } else {
                        EClassContent stored = storedItem.get(0);
                        if (!content.equals(stored)) { // 뭔가 바뀐 경우.
                            contentsDAO.update(content);
                        }
                    }
                    minIndex = minIndex < itemIndex ? minIndex : itemIndex;
                    // todo, return true를 만들기 위해 minIndex를 활용할 것.
                }
                return false; // 일단 false...
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
                return localProperties.getChecked(boardIndex);
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
