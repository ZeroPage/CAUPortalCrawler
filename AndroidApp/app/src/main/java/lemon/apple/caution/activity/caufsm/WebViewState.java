package lemon.apple.caution.activity.caufsm;

import android.os.Environment;
import android.webkit.WebView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lemon.apple.caution.jsoupaser.CAUParseException;
import lemon.apple.caution.jsoupaser.CAUParser;
import lemon.apple.caution.jsoupaser.ParserFactory;
import lemon.apple.caution.model.EClassContent;
import timber.log.Timber;

enum WebViewState {
    START {
        @Override
        public void invoke(WebView webView) {
            openURL(webView, "http://portal.cau.ac.kr");
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            if (url.startsWith("http://portal.cau.ac.kr/_layouts/Cau/Member/Login.aspx")) {
                helper.setState(webView, LOGIN);
                LOGIN.invoke(webView);
            } else if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                helper.setState(webView, MAIN);
                MAIN.invoke(webView);
            }
        }
    },
    LOGIN {
        @Override
        public void invoke(WebView webView) {
            StringBuilder jsBuilder = new StringBuilder()
                    .append(String.format("document.querySelector('#txtUserID').value='%s';", helper.getPortalId()))
                    .append(String.format("document.querySelector('#txtUserPwd').value='%s';", helper.getPassword()));
            jsBuilder.append("document.querySelector('#btnLogin').click();");
            runJS(webView, jsBuilder.toString(), "void");
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            if (url.startsWith("http://portal.cau.ac.kr/pages/mydefault.aspx")) {
                helper.setState(webView, MAIN);
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
                helper.setState(webView, ECLASS);
                ECLASS.invoke(webView);
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
                helper.setState(webView, ECLASS_LIST);
                ECLASS_LIST.invoke(webView);
            }
        }
    },
    ECLASS_LIST {

        private final String existTest = "existTest";

        @Override
        public void invoke(WebView webView) {
            Timber.d("just wait." + webView.getUrl());
        }

        @Override
        public void onProgressChanged(WebView webView) {
            String script = "document.querySelector('#contentFrame').contentWindow.document" +
                    ".querySelectorAll('#infomationCourse_body_tbody > tr > td:first-child > nobr')"
                    + ".length";
            runJS(webView, script, existTest);
        }

        @Override
        public void onJsAlert(WebView webView, String key, String android_val) {
            if (key.equals(existTest)) {
                int numberOfLecture = Integer.parseInt(android_val);
                if (numberOfLecture != 0) {
                    helper.setLectureMax(numberOfLecture);
                    helper.initLectureIndex();
                    enterLecture(webView);
                }
            } else if (key.equals("close")) {
                int lectureIndex = helper.getLectureIndexNext();
                if (lectureIndex < helper.getLectureMax()) {
                    enterLecture(webView);
                } else {
                    helper.setState(webView, FINAL);
                }
            }
        }

        private void enterLecture(WebView webView) {
            String script = "document.querySelector('#contentFrame').contentWindow.document" +
                    ".querySelectorAll('#infomationCourse_body_tbody > tr > td:first-child > nobr')"
                    + "[" + helper.getLectureIndex() + "].click()";
            runJS(webView, script, "void");
            helper.setState(webView, LECTURE_MAIN);
        }
    },
    LECTURE_MAIN {
        private final String boardExist = "boardIdExist";
        private final String boardEnter = "boardEnter";

        @Override
        public void onProgressChanged(WebView webView) {
            String script = "document.querySelector('#menuFrame').contentWindow.document" +
                    ".querySelectorAll('#repeat5_1_repeat6 > table > tbody > tr:nth-child(1) > td > div > div')"
                    + "!= null";
            runJS(webView, script, boardExist, 100);
        }

        @Override
        public void onJsAlert(WebView webView, String key, String android_val) {
            if (key.equals(boardExist)) {
                if (Boolean.parseBoolean(android_val)) {
                    //init
                    helper.initBoardIndex();
                    enterBoard(webView);
                }
            } else if (key.equals(boardEnter)) {
                helper.setState(webView, LECTURE_CRAWL);
                runJS(webView, "'touch'", LECTURE_CRAWL.name());
            } else if (key.equals(this.name())) {
                int boardIndex = helper.getBoardIndexNext();
                if (boardIndex < CAUParser.MAX_BOARD_BOUND)
                    if (helper.isAllowedBoard(boardIndex)) {
                        enterBoard(webView);
                    } else {
                        runJS(webView, "'next'", this.name());
                    }
                else {
                    helper.setState(webView, ECLASS_LIST);
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
                    + "[" + helper.getBoardIndex() + "].click()";
            runJS(webView, script, boardEnter, 100);
        }
    },
    LECTURE_CRAWL {
        private final String crawling = "crawling";
        private final String ping = "ping";

        @Override
        public void onProgressChanged(WebView webView) {
            ping(webView);
        }

        private void ping(WebView webView) {
            String script = "document.querySelector('#contentFrame').contentWindow.document" +
                    ".querySelector('table.grid_header>tbody').children.length";
            runJS(webView, script, ping);
        }

        @Override
        public void onJsAlert(WebView webView, String key, String android_val) {
            if (key.equals(crawling)) {
                if (helper.isHaveToCrawl()) {
                    try {
                        String html = String.format("<table><tbody>%s</tbody></table>", android_val);

                        // test.
                        File directory = Environment.getExternalStorageDirectory();
                        String fileName = "table[" + helper.getLectureIndex() + "," + helper.getBoardIndex() + "].html";
                        File file = new File(directory, fileName);
                        FileUtils.writeStringToFile(file, html);
                        try {
                            CAUParser parser = ParserFactory.createParser(helper.getBoardIndex());
                            List<EClassContent> contents = parser.parse(html);
                            for (EClassContent content : contents) {
                                content.setLecture(helper.getLectureIndex());
                            }

                            boolean isNeedMore = helper.storeResult(contents);
                            if (isNeedMore) {
                                // todo, 파싱하다 깨닳은건데, 페이지 넘겨야한다. 시발.
                                // todo more state or js need...
                            }
                        } catch (CAUParseException ignored) {
                            // todo, 일단 무시.
                        }
                    } catch (IOException e) {
                        Timber.e(e, "FILE IO EXCEPTION: At LECTURE_CRAWL ");
                    } finally {
                        helper.setState(webView, LECTURE_MAIN);
                        runJS(webView, "'next'", LECTURE_MAIN.name());
                    }
                } else {
                    int itemIndex = helper.getItemIndex();
                    while (helper.getLectureIndex() >= helper.getLectureMax()) {
                        helper.getLectureIndexNext(); // to terminate state machine.
                    }
                    helper.setState(webView, ECLASS_LIST);

                    String script = "Array.prototype.filter.call(" +
                            "document.querySelector('#contentFrame').contentWindow.document" +
                            ".querySelectorAll('table.grid_header>tbody>tr:not(.w2grid_hidedRow)')," +
                            "function (val){" +
                            "return val.querySelectorAll('nobr')[0].innerText==" + itemIndex + ";" +
                            "})[0].querySelectorAll('nobr')[1].click();";
                    runJS(webView, script, "close");
                }
            } else if (key.equals(ping)) {
                if (Integer.parseInt(android_val) != 11) {
                    ping(webView);
                } else {
                    String script = "document.querySelector('#contentFrame').contentWindow.document" +
                            ".querySelector('table.grid_header>tbody').innerHTML";
                    runJS(webView, script, crawling, 300);
                }
            } else if (key.equals(this.name())) {
                ping(webView);
//                String script = "document.querySelector('#contentFrame').contentWindow.document" +
//                        ".querySelector('table.grid_header').innerHTML";
//                runJS(webView, script, crawling, 5000);
            }
        }
    },
    FINAL;

    /**
     * do nothing. dummy listener.
     */
    private static StateHelper helper = new DummyStateHelper();

    private static void openURL(WebView webView, String url) {
        webView.loadUrl(url);
    }

    private static void runJS(final WebView webView, final String script, final String tag) {
        runJS(webView, script, tag, 5);
    }

    private static void runJS(final WebView webView, final String script, final String tag, int delayMillis) {
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                String js = "javascript:" +
                        "var android_var=" + script + ";" +
                        "alert('@" + tag + ":'+android_var);";
                webView.loadUrl(js);
            }
        }, delayMillis);
    }

    public static void runJSPublic(WebView webView, String script, String tag) { // fixme
        runJS(webView, script, tag);
    }

    public static void setHelper(StateHelper helper) {
        WebViewState.helper = helper;
    }

    public static void start(WebView webView) {
        helper.init();
        helper.setState(webView, START);
        START.invoke(webView);
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
        start(webView);
    }

    public static interface StateHelper {
        void setState(WebView webView, WebViewState changeTo);

        void initBoardIndex();

        int getBoardIndex();

        void initLectureIndex();

        int getLectureIndex();

        int getLectureMax();

        void setLectureMax(int numberOfLecture);

        boolean storeResult(List<EClassContent> contents);

        String getPortalId();

        String getPassword();

        boolean isAllowedBoard(int boardIndex);

        boolean isHaveToCrawl();

        int getItemIndex();

        int getBoardIndexNext();

        int getLectureIndexNext();

        void init();

    }

}
// fixme, public들 전부 package local로...