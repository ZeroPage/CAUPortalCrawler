package test.apple.lemon.cauportalcrawlertest.activity.caufsm;

import android.webkit.WebView;

import java.util.List;

import test.apple.lemon.cauportalcrawlertest.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 7..
 */
class DummyStateHelper implements WebViewState.StateHelper {
    @Override
    public void setState(WebView webView, WebViewState changeTo) {

    }

    @Override
    public void initBoardIndex() {

    }

    @Override
    public int getBoardIndex() {
        return 0;
    }

    @Override
    public void setBoardIndex(int newIndex) {

    }

    @Override
    public void initLectureIndex() {

    }

    @Override
    public int getLectureIndex() {
        return 0;
    }

    @Override
    public void setLectureIndex(int newIndex) {

    }

    @Override
    public int getLectureMax() {
        return 0;
    }

    @Override
    public void setLectureMax(int numberOfLecture) {

    }

    @Override
    public boolean storeResult(List<EClassContent> contents) {
        return false;
    }

    @Override
    public String getPortalId() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAllowedBoard(int boardIndex) {
        return false;
    }
}
