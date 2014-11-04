package test.apple.lemon.cauportalcrawlertest;

import android.os.Environment;
import android.webkit.WebView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 10. 2..
 */
public enum State {
    UNKNOWN,
    START {
        @Override
        public void invoke(WebView webView) {
            openURL(webView, "http://portal.cau.ac.kr");
        }

        @Override
        public State onPageFinished(WebView webView, String url) {
            if (url.startsWith("http://portal.cau.ac.kr/_layouts/Cau/Member/Login.aspx")) {
                return LOGIN;
            } else if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                return MAIN;
            } else {
                return UNKNOWN;
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
        public State onPageFinished(WebView webView, String url) {
            if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                return MAIN;
            } else {
                return this;
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
        public State onPageFinished(WebView webView, String url) {
            return url.startsWith(nextUrl) ? ECLASS : UNKNOWN;
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
        public State onPageFinished(WebView webView, String url) {
            if (url.startsWith(nextUrl)) {
                return ECLASS_LIST;
            } else {
                return UNKNOWN;
            }
        }
    },
    ECLASS_LIST {

        private final String tag = "infomationCourse_body_tbody";

        @Override
        public void invoke(WebView webView) {
            Timber.d("just wait.");
        }

        @Override
        public State onProgressChanged(WebView webView) {
            String script = "document.querySelector('#contentFrame').contentWindow.document" +
                    ".querySelectorAll('#infomationCourse_body_tbody > tr > td:first-child > nobr')"
                    + ".length";
            runJS(webView, script, tag);
            return this;
        }

        @Override
        public State onJsAlert(WebView webView, String key, String android_val) {
            if (key.equals(tag)) {
                int i = Integer.parseInt(android_val);
                if (i != 0) {
                    String script = "document.querySelector('#contentFrame').contentWindow.document" +
                            ".querySelectorAll('#infomationCourse_body_tbody > tr > td:first-child > nobr')"
                            + "[0].click()";
                    // todo 적절히 입장...
                    runJS(webView, script, "void");
                    return LECTURE_MAIN;
                }
            } else if (key.equals("close")) {
                // todo 다음 강의.

                // 지금은 그냥 종료.
                stateListener.onFinalState();
                return FINAL;
            }
            return this;
        }
    },
    LECTURE_MAIN {
        private final String boardExist = "boardIdExist";
        private final String boardEnter = "boardEnter";

        private int boardIndex = 0;

        @Override
        public State onProgressChanged(WebView webView) {
            String script = "document.querySelector('#menuFrame').contentWindow.document" +
                    ".querySelectorAll('#repeat5_1_repeat6 > table > tbody > tr:nth-child(1) > td > div > div')"
                    + "!= null";
            runJS(webView, script, boardExist);
            return this;
        }

        @Override
        public State onJsAlert(WebView webView, String key, String android_val) {
            if (key.equals(boardExist)) {
                if (Boolean.parseBoolean(android_val)) {
                    //init
                    boardIndex = 0;
                    enterBoard(webView);
                    return this;
                }
            } else if (key.equals(boardEnter)) { // load 되길 기다린다.
                runJS(webView, "" + boardIndex, LECTURE_CRAWL.name());
                return LECTURE_CRAWL;
            } else if (key.equals(this.name())) {
                boardIndex++;
                if (boardIndex < 6) {
                    enterBoard(webView);
                    return this;
                } else {
                    runJS(webView, "document.querySelector('#imgClose').click();", "see_WebChromeClient.onClose");
                    return ECLASS_LIST;
                }
            }
            return super.onJsAlert(webView, key, android_val);
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
        private final String deterNext = "deterNext";

        private String boardIndex = "unknown";

        @Override
        public State onProgressChanged(WebView webView) {
            String script = "document.querySelector('#contentFrame').contentWindow.document" +
                    ".querySelector('table.grid_header').innerHTML";
            runJS(webView, script, deterNext);
            return this;
        }

        @Override
        public State onJsAlert(WebView webView, String key, String android_val) {
            if (key.equals(deterNext)) {
                try {
                    String data = String.format("<table class=\"grid_header\">%s</table>", android_val);
                    File directory = Environment.getExternalStorageDirectory();
                    FileUtils.writeStringToFile(new File(directory, "table[" + boardIndex + "].html"), data);
                } catch (IOException e) {
                    Timber.e(e, "FILE IO EXCEPTION: At LECTURE_CRAWL ");
                } finally {
                    runJS(webView, "'next'", LECTURE_MAIN.name()); // fixme, 이 방법으로 state 옮기는거 가끔 이탈함.
                }
                return LECTURE_MAIN;
            } else if (key.equals(this.name())) {
                boardIndex = android_val;
                return this;
            } else if (key.equals(LECTURE_MAIN.name())) { // fixme, 일단 state 가 넘어갈 때까지 재시도.
                runJS(webView, "'next'", LECTURE_MAIN.name()); // fixme, 이 방법으로 state 옮기는거 가끔 이탈함.
                return LECTURE_MAIN;
            } else if (key.equals("boardEnter")) { // fixme....
                return this;
            } else {
                Timber.d("asdf");
            }
            return super.onJsAlert(webView, key, android_val);
        }
    },
    FINAL {
        @Override
        public void invoke(WebView webView) {
            stateListener.onFinalState();
        }

        @Override
        public State onJsAlert(WebView webView, String key, String android_val) {
            stateListener.onFinalState();
            return super.onJsAlert(webView, key, android_val);
        }
    };
    private static StateListener stateListener = new StateListener() {
        @Override
        public void onFinalState() {
            // do nothing. dummy listener.
        }
    };

    private static void openURL(WebView webView, String url) {
        webView.loadUrl(url);
    }

    private static void runJS(WebView webView, String script, String tag) {
        String js = "javascript:" +
                "var android_var=" + script + ";" +
                "alert('@" + tag + ":'+android_var);";
        webView.loadUrl(js);
    }

    public static void runJSpub(WebView webView, String script, String tag) { // fixme
        runJS(webView, script, tag);
    }

    public static void setStateListener(StateListener stateListener) {
        State.stateListener = stateListener;
    }

    public void invoke(WebView webView) {

    }

    public State onPageFinished(WebView webView, String url) { //receiveURL
        Timber.d(url);
        return this;
    }

    public State onProgressChanged(WebView webView) { //checkIFrameLoaded
        return this;
    }

    public State onJsAlert(WebView webView, String key, String android_val) { //resultOfJsForKey
        return this;
    }

    public State onTimeout(WebView webView) {
        return START;
    }

    public static interface StateListener {
        void onFinalState();
    }
}
