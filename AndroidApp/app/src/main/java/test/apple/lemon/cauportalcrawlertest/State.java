package test.apple.lemon.cauportalcrawlertest;

import android.webkit.WebView;

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
            runJS(webView, jsBuilder.toString());
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
            if (tag.equals(key)) {
                int i = Integer.parseInt(android_val);
                if (i != 0) {
                    String script = "document.querySelector('#contentFrame').contentWindow.document" +
                            ".querySelectorAll('#infomationCourse_body_tbody > tr > td:first-child > nobr')"
                            + "[0].click()";
                    runJS(webView, script);
                    return LECTURE_MAIN;
                }
            }
            return this;
        }
    },
    LECTURE_MAIN {
        private final String tag = "boardIdExist";

        @Override
        public State onProgressChanged(WebView webView) {
            String script = "document.querySelector('#menuFrame').contentWindow.document" +
                    ".querySelectorAll('#repeat5_1_repeat6 > table > tbody > tr:nth-child(1) > td > div > div')"
                    + "!= null";
            runJS(webView, script, tag);
            return this;
        }

        @Override
        public State onJsAlert(WebView webView, String key, String android_val) {
            if (tag.equals(key)) {
                if (Boolean.parseBoolean(android_val)) {

                    String script = "document.querySelector('#menuFrame').contentWindow.document" +
                            ".querySelectorAll('#repeat5_1_repeat6 > table > tbody > tr > td > div > div.depth3_out')"
                            + "[0].click()";
                    runJS(webView, script);
                    // 0 공지사항
                    // 1 강의콘텐츠
                    // 2 과제방
                    // 3 팀프로젝트
                    // 4 공유자료실
                    // 5 과목Q&A
                    // 6 노트정리 // 안 해
                    // 7 학습관리 // 안 해
                    return LECTURE_LOOKUP;
                }
            }
            return super.onJsAlert(webView, key, android_val);
        }
    },
    LECTURE_LOOKUP {
        @Override
        public State onProgressChanged(WebView webView) {
            runJS(webView, "document.querySelector('#imgClose').click();", "close");
            return this;
        }

        @Override
        public State onJsAlert(WebView webView, String key, String android_val) {
            if (key.equals("close")) {
                return UNKNOWN;
            }
            return super.onJsAlert(webView, key, android_val);
        }
    },;

    private static void openURL(WebView webView, String url) {
        webView.loadUrl(url);
    }

    private static void runJS(WebView webView, String script) {
        runJS(webView, script, "void");
    }

    private static void runJS(WebView webView, String script, String tag) {
        String js = "javascript:" +
                "var android_var=" + script + ";" +
                "alert('@" + tag + ":'+android_var);";
        webView.loadUrl(js);
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
}
