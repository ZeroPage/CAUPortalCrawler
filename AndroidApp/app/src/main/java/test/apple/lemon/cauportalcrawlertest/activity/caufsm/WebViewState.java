package test.apple.lemon.cauportalcrawlertest.activity.caufsm;

import android.os.Environment;
import android.webkit.WebView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import test.apple.lemon.cauportalcrawlertest.Pref;
import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 10. 2..
 */
enum WebViewState {
    UNKNOWN,
    START {
        @Override
        public void invoke(WebView webView) {
            openURL(webView, "http://portal.cau.ac.kr");
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            if (url.startsWith("http://portal.cau.ac.kr/_layouts/Cau/Member/Login.aspx")) {
                stateListener.onStateChange(LOGIN);
                LOGIN.invoke(webView);
            } else if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                stateListener.onStateChange(MAIN);
                MAIN.invoke(webView);
            } else {
                stateListener.onStateChange(UNKNOWN);
                UNKNOWN.invoke(webView);
            }
        }
    },
    LOGIN {
        @Override
        public void invoke(WebView webView) {
            StringBuilder jsBuilder = new StringBuilder()
                    .append(String.format("document.querySelector('#txtUserID').value='%s';", Pref.ID))
                    .append(String.format("document.querySelector('#txtUserPwd').value='%s';", Pref.PASSWD));
            jsBuilder.append("document.querySelector('#btnLogin').click();");
            runJS(webView, jsBuilder.toString(), "void");
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                stateListener.onStateChange(MAIN);
                MAIN.invoke(webView);
            }
        }
    },
    MAIN {
        private final String nextUrl = "http://portal.cau.ac.kr/Eclass/Pages/e_class.aspx";

        @Override
        public void invoke(WebView webView) {
            openURL(webView, nextUrl);
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            if (url.startsWith(nextUrl)) {
                stateListener.onStateChange(ECLASS);
                ECLASS.invoke(webView);
            } else {
                stateListener.onStateChange(UNKNOWN);
                UNKNOWN.invoke(webView);
            }
        }
    },
    ECLASS {
        // private String nextUrl_prefix = "http://cautis.cau.ac.kr/LMS/websquare/websquare.jsp";
        private final String nextUrl = "http://cautis.cau.ac.kr/LMS/websquare/websquare.jsp?w2xPath=/LMS/comm/main.xml";

        @Override
        public void invoke(WebView webView) {
            // runJS(webView, "$('#External_Content_IFrame').attr('src');"); // 만일 이클래스 강의실 페이지가 사용자마다 다른 동적 값일 경우.
            openURL(webView, nextUrl);
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            if (url.startsWith(nextUrl)) {
                stateListener.onStateChange(ECLASS_LIST);
                ECLASS_LIST.invoke(webView);
            } else {
                stateListener.onStateChange(UNKNOWN);
                UNKNOWN.invoke(webView);
            }
        }
    },
    ECLASS_LIST {

        private final String tag = "infomationCourse_body_tbody"; // todo, rename and rewrite tag.

        @Override
        public void invoke(WebView webView) {
            Timber.d("just wait.");
        }

        @Override
        public void onProgressChanged(WebView webView) {
            String script = "document.querySelector('#contentFrame').contentWindow.document" +
                    ".querySelectorAll('#infomationCourse_body_tbody > tr > td:first-child > nobr')"
                    + ".length";
            runJS(webView, script, tag);
        }

        @Override
        public void onJsAlert(WebView webView, String key, String android_val) {
            if (key.equals(tag)) {
                int i = Integer.parseInt(android_val);
                if (i != 0) {
                    String script = "document.querySelector('#contentFrame').contentWindow.document" +
                            ".querySelectorAll('#infomationCourse_body_tbody > tr > td:first-child > nobr')"
                            + "[0].click()";
                    // todo 적절히 입장...
                    runJS(webView, script, "void");
                    stateListener.onStateChange(LECTURE_MAIN);
                }
            } else if (key.equals("close")) {
                // todo 다음 강의.

                // 지금은 그냥 종료.
                stateListener.onFinalState();
                stateListener.onStateChange(FINAL);
            }
        }
    },
    LECTURE_MAIN {
        private final String boardExist = "boardIdExist";
        private final String boardEnter = "boardEnter";

        private int boardIndex = 0;

        @Override
        public void onProgressChanged(WebView webView) {
            String script = "document.querySelector('#menuFrame').contentWindow.document" +
                    ".querySelectorAll('#repeat5_1_repeat6 > table > tbody > tr:nth-child(1) > td > div > div')"
                    + "!= null";
            runJS(webView, script, boardExist);
        }

        @Override
        public void onJsAlert(WebView webView, String key, String android_val) {
            if (key.equals(boardExist)) {
                if (Boolean.parseBoolean(android_val)) {
                    //init
                    boardIndex = 0;
                    enterBoard(webView);
                }
            } else if (key.equals(boardEnter)) { // load 되길 기다린다.
                runJS(webView, "" + boardIndex, LECTURE_CRAWL.name()); // fixme key.
                stateListener.onStateChange(LECTURE_CRAWL);
            } else if (key.equals(this.name())) {
                boardIndex++;
                if (boardIndex < 6) {
                    enterBoard(webView);
                } else {
                    stateListener.onStateChange(ECLASS_LIST);
                    runJS(webView, "document.querySelector('#imgClose').click();", "see_WebChromeClient.onClose");
                }
            }
        }

        private void enterBoard(WebView webView) {
            // 0 공지사항
            // 1 강의콘텐츠
            // 2 과제방
            // 3 팀프로젝트
            // 4 공유자료실
            // 5 과목Q&A
            // 6 노트정리 // 안 해
            // 7 학습관리 // 안 해
            String script = "document.querySelector('#menuFrame').contentWindow.document" +
                    ".querySelectorAll('#repeat5_1_repeat6 > table > tbody > tr > td > div > div.depth3_out')"
                    + "[" + boardIndex + "].click()";
            // todo 적절히 입장...
            runJS(webView, script, boardEnter);
        }
    },
    LECTURE_CRAWL {
        private final String crawling = "crawling";

        private String boardIndex = "unknown";

        @Override
        public void onProgressChanged(WebView webView) {
            // fixme, onJsAlert보다 이게 먼저 호출되는 경우 문제가 발생.
            String script = "document.querySelector('#contentFrame').contentWindow.document" +
                    ".querySelector('table.grid_header').innerHTML";
            runJS(webView, script, crawling);
        }

        @Override
        public void onJsAlert(WebView webView, String key, String android_val) { // fixme, 바뀐 state 변경 정책에 따라 맞출 것.
            if (key.equals(crawling)) {
                try {
                    String data = String.format("<table class=\"grid_header\">%s</table>", android_val);
                    File directory = Environment.getExternalStorageDirectory();
                    FileUtils.writeStringToFile(new File(directory, "table[" + boardIndex + "].html"), data);
                } catch (IOException e) {
                    Timber.e(e, "FILE IO EXCEPTION: At LECTURE_CRAWL ");
                } finally {
                    stateListener.onStateChange(LECTURE_MAIN);
                    runJS(webView, "'next'", LECTURE_MAIN.name()); // fixme, 이 방법으로 state 옮기는거 가끔 이탈함.
                }
            } else if (key.equals(this.name())) {
                // set Index
                boardIndex = android_val;
            }
        }
    },
    FINAL;
    private static StateListener stateListener = new StateListener() {
        @Override
        public void onStartState() {

        }

        @Override
        public void onFinalState() {
            // do nothing. dummy listener.
        }

        @Override
        public void onStateChange(WebViewState changeTo) {
            // do nothing. dummy listener.
        }
    };

    private static void openURL(WebView webView, String url) {
        webView.loadUrl(url);
    }

    private static void runJS(final WebView webView, final String script, final String tag) {
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String js = "javascript:" +
                        "var android_var=" + script + ";" +
                        "alert('@" + tag + ":'+android_var);";
                webView.loadUrl(js);
            }
        }, 50); // 약간의 갭을 줘서 이탈을 조금이라도 방지.
    }

    public static void runJSPublic(WebView webView, String script, String tag) { // fixme
        runJS(webView, script, tag);
    }

    public static void setStateListener(StateListener stateListener) {
        WebViewState.stateListener = stateListener;
    }

    public void invoke(WebView webView) {

    }

    public void onPageFinished(WebView webView, String url) { //receiveURL
        Timber.d(String.format("onPageFinished @ %s @ %s", name(), url));
    }

    public void onProgressChanged(WebView webView) { //checkIFrameLoaded
    }

    public void onJsAlert(WebView webView, String key, String android_val) { //resultOfJsForKey
    }

    public void onTimeout(WebView webView) {
        stateListener.onStateChange(START);
        START.invoke(webView);
    }

    public static void start(WebView webView) {
        stateListener.onStateChange(START);
        START.invoke(webView);
        stateListener.onStartState();
    }

    public static interface StateListener {
        void onStartState();

        void onFinalState();

        void onStateChange(WebViewState changeTo); // todo rename to setState.
    }
}
// fixme, public들 전부 package local로...