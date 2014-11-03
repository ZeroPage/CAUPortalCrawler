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
        public void invoke(WebView webView) {
            webView.loadUrl("http://portal.cau.ac.kr");
            //        mWebView.loadUrl("http://cautis.cau.ac.kr/SMT/main.jsp"); // 모바일 페이지. 좀더 편리 할듯하나
        }

        @Override
        public State onPageFinished(WebView webView, String url) {
            if (url.startsWith("http://portal.cau.ac.kr/_layouts/Cau/Member/Login.aspx")) {
                return LOGIN;
            } else if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                return MAIN;
            } else {
                return TRASH;
            }
        }
    },
    LOGIN {
        @Override
        public void invoke(WebView webView) {
            runJS(webView,
                    "not_use", "$('#txtUserID').val('" + Pref.ID + "');" +
                            "$('#txtUserPwd').val('" + Pref.PASSWD + "');" +
                            "$('#btnLogin').click();");
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

        private String nextUrl;

        @Override
        public void invoke(WebView webView) {
            nextUrl = "http://portal.cau.ac.kr/Eclass/Pages/e_class.aspx";
            webView.loadUrl(nextUrl);
        }

        @Override
        public State onPageFinished(WebView webView, String url) {
            return url.startsWith(nextUrl) ? ECLASS : TRASH;
        }
    },
    ECLASS {
        private String nextUrl_prefix = "http://cautis.cau.ac.kr/LMS/websquare/websquare.jsp";
        private String nextUrl = "http://cautis.cau.ac.kr/LMS/websquare/websquare.jsp?w2xPath=/LMS/comm/main.xml";

        @Override
        public void invoke(WebView webView) {
            //            runJS(webView, "$('#External_Content_IFrame').attr('src');"); // 만일 이클래스 강의실 페이지가 사용자마다 다른 동적 값일 경우.
            webView.loadUrl(nextUrl);
        }

        @Override
        public State onPageFinished(WebView webView, String url) {
            if (url.startsWith(nextUrl_prefix)) {
                return ECLASS_LIST;
            } else {
                return TRASH;
            }
        }
    },
    ECLASS_LIST {

        private final String frame = "contentFrame";

        @Override
        public State onProgressChanged(WebView webView) {
            runJS(webView, frame, "$('#contentFrame').contents().find('#infomationCourse_body_tbody > tr > td:first-child').length;");
            return this;
        }

        @Override
        public State onJsAlert(WebView webView, String key, String android_val) {
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
        public State onProgressChanged(WebView webView) {
            runJS(webView, frame, "$('#menuFrame').contents().find('#repeat5_1_repeat6 > table > tbody > tr:nth-child(1) > td > div > div') != null");
            return this;
        }

        @Override
        public State onJsAlert(WebView webView, String key, String android_val) {
            if (frame.equals(key)) {
                if (Boolean.parseBoolean(android_val)) {
                    runJS(webView, "not_use", "$('#menuFrame').contents().find('#repeat5_1_repeat6 > table > tbody > tr:nth-child(1) > td > div > div').click();");
                    // $('#menuFrame').contentDocument.querySelectorAll('#repeat5_1_repeat6_0_group7 > div')[0].click();
                    // text를 가지고 있는 녀석을 찾아서 click 하면 됨.
                    return TRASH;
                }
            }
            return super.onJsAlert(webView, key, android_val);
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
