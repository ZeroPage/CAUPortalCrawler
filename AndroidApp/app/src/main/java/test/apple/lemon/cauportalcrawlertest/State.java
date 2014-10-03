package test.apple.lemon.cauportalcrawlertest;

import android.webkit.WebView;

import timber.log.Timber;

/**
* Created by rino0601 on 2014. 10. 2..
*/
public enum State {
    TRASH {

    },
    START {
        @Override
        public State receiveURL(WebView webView, String url) {
            if (url.startsWith("http://portal.cau.ac.kr/_layouts/Cau/Member/Login.aspx")) {
                return LOGIN;
            } else if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                return MAIN;
            } else {
                return TRASH;
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
                    "not_use", "$('#txtUserID').val('" + Pref.ID + "');" +
                            "$('#txtUserPwd').val('" + Pref.PASSWD + "');" +
                            "$('#btnLogin').click();");
        }
    },
    MAIN {

        private String nextUrl;

        @Override
        public State receiveURL(WebView webView, String url) {
            return url.startsWith(nextUrl) ? ECLASS : TRASH;
        }

        @Override
        public void process(WebView webView) {
            nextUrl = "http://portal.cau.ac.kr/Eclass/Pages/e_class.aspx";
            webView.loadUrl(nextUrl);
        }
    },
    ECLASS {
        private String nextUrl_prefix = "http://cautis.cau.ac.kr/LMS/websquare/websquare.jsp";
        private String nextUrl = "http://cautis.cau.ac.kr/LMS/websquare/websquare.jsp?w2xPath=/LMS/comm/main.xml";

        @Override
        public State receiveURL(WebView webView, String url) {
            if (url.startsWith(nextUrl_prefix)) {
                return ECLASS_LIST;
            } else {
                return TRASH;
            }
        }

        @Override
        public void process(WebView webView) {
            //            runJS(webView, "$('#External_Content_IFrame').attr('src');"); // 만일 이클래스 강의실 페이지가 사용자마다 다른 동적 값일 경우.
            webView.loadUrl(nextUrl);
        }
    },
    ECLASS_LIST {

        private final String frame = "contentFrame";

        @Override
        public void checkIFrameLoaded(WebView webView) {
            runJS(webView, frame, "$('#contentFrame').contents().find('#infomationCourse_body_tbody > tr > td:first-child').length;");
        }

        @Override
        public State resultOfJsForKey(WebView webView, String key, String android_val) {
            if (frame.equals(key)) {
                int i = Integer.parseInt(android_val);
                if (i != 0) {
                    runJS(webView, "not_use", "$('#contentFrame').contents().find('#infomationCourse_body_tbody > tr > td:nth-child(1)').click();");
                    return LECTURE_MAIN;
                }
            }
            return this;
        }
    },
    LECTURE_MAIN {
        private final String frame = "menuFrame";

        @Override
        public void checkIFrameLoaded(WebView webView) {
            runJS(webView, frame, "$('#menuFrame').contents().find('#repeat5_1_repeat6 > table > tbody > tr:nth-child(1) > td > div > div') != null");
        }

        @Override
        public State resultOfJsForKey(WebView webView, String key, String android_val) {
            if (frame.equals(key)) {
                if (Boolean.parseBoolean(android_val)) {
                    runJS(webView, "not_use", "$('#menuFrame').contents().find('#repeat5_1_repeat6 > table > tbody > tr:nth-child(1) > td > div > div').click();");
                    // $('#menuFrame').contentDocument.querySelectorAll('#repeat5_1_repeat6_0_group7 > div')[0].click();
                    // text를 가지고 있는 녀석을 찾아서 click 하면 됨.
                    return TRASH;
                }
            }
            return super.resultOfJsForKey(webView, key, android_val);
        }
    },
    LECTURE_NOTICE,
    LECTURE_CONTENT,
    LECTURE_HW,
    LECTURE_SHARE;

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

    public State onTimeout() {
        return START;
    }
}
