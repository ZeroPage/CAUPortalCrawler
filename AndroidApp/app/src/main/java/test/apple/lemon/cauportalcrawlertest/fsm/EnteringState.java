package test.apple.lemon.cauportalcrawlertest.fsm;

import android.webkit.WebView;

import test.apple.lemon.cauportalcrawlertest.Pref;
import timber.log.Timber;

/**
 * Created by rino0601 on 2014. 9. 25..
 */
public enum EnteringState {
    UNKNOWN,
    START {
        @Override
        public EnteringState receiveURL(String url) {
            if (url.startsWith("http://portal.cau.ac.kr/_layouts/Cau/Member/Login.aspx")) {
                return LOGIN;
            } else if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                return MAIN;
            } else {
                return super.receiveURL(url);
            }
        }
    },
    LOGIN {
        private String ID;private String PASSWD;

        @Override
        public EnteringState receiveURL(String url) {
            if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                return MAIN;
            } else {
                return super.receiveURL(url);
            }
        }

        @Override
        public void doAction(WebView webView) {
            runJS(webView,
                    "$('#txtUserID').val('" + Pref.ID + "');" +
                            "$('#txtUserPwd').val('" + Pref.PASSWD + "');" +
                            "$('#btnLogin').click();");
        }
    },
    MAIN {

        private String nextUrl;

        @Override
        public void doAction(WebView webView) {
            super.doAction(webView);
            nextUrl = "http://portal.cau.ac.kr/Eclass/Pages/e_class.aspx";
            webView.loadUrl(nextUrl);
        }

        @Override
        public EnteringState receiveURL(String url) {
            return url.startsWith(nextUrl) ? ECLASS : super.receiveURL(url);
        }
    },
    ECLASS {
        private String nextUrl;

        @Override
        public void doAction(WebView webView) {
//            runJS(webView, "$('#External_Content_IFrame').attr('src');"); // 만일 이클래스 강의실 페이지가 사용자마다 다른 동적 값일 경우.
            nextUrl = "http://cautis.cau.ac.kr/LMS/websquare/websquare.jsp";
            webView.loadUrl(nextUrl + "?w2xPath=/LMS/comm/main.xml");
            super.doAction(webView);
        }

        @Override
        public EnteringState receiveURL(String url) {
            if (url.startsWith(nextUrl)) {
                return ECLASS_LIST;
            } else {
                return super.receiveURL(url);
            }
        }
    }, ECLASS_LIST {
        @Override
        public void doAction(WebView webView) {
            runJS(webView,"$('#contentFrame').contents().find('#infomationCourse_body_tbody > tr > td:first-child')[0]");
            super.doAction(webView);
        }

        @Override
        public EnteringState receiveURL(String url) {
            return super.receiveURL(url);
        }
    };

    private static void runJS(WebView webView, String script) {
        String js = "javascript:" +
                script;
        webView.loadUrl(js);
    }


    public void doAction(WebView webView) {
        Timber.d("doAction");
    }

    public EnteringState receiveURL(String url) {
        Timber.d(url);
        return UNKNOWN;
    }
}
