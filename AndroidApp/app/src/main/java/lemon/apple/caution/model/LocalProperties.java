package lemon.apple.caution.model;

import java.util.ArrayList;
import java.util.HashMap;

import lemon.apple.caution.jsoupaser.HomeworkParser;
import lemon.apple.caution.jsoupaser.LectureContentsParser;
import lemon.apple.caution.jsoupaser.NoticeParser;
import lemon.apple.caution.jsoupaser.QnAParser;
import lemon.apple.caution.jsoupaser.SharedDataParser;
import lemon.apple.caution.jsoupaser.TeamProjectParser;

/**
 * Created by rino0601 on 2014. 11. 13..
 */
public class LocalProperties {

    private String password;
    private String portalId;
    private Integer scheduledHour;
    private Integer scheduledMinute;
    private HashMap<Integer, Boolean> boardCheck;
    private ArrayList<String> lecuterName;


    public LocalProperties() { // default value
        password = "";
        portalId = "";
        scheduledHour = 5;
        scheduledMinute = 0;
        boardCheck = new HashMap<Integer, Boolean>();
        boardCheck.put(NoticeParser.BOARD_INDEX, true);
        boardCheck.put(LectureContentsParser.BOARD_INDEX, false);
        boardCheck.put(HomeworkParser.BOARD_INDEX, true);
        boardCheck.put(TeamProjectParser.BOARD_INDEX, false);
        boardCheck.put(SharedDataParser.BOARD_INDEX, true);
        boardCheck.put(QnAParser.BOARD_INDEX, true);
        lecuterName = new ArrayList<String>();
        lecuterName.add("21세기글로벌리즘의이해 (01)");
        lecuterName.add("LINUX시스템 (01)");
        lecuterName.add("모바일 앱 개발 (01)");
        lecuterName.add("산업체특강(2) (01)");
        lecuterName.add("컴퓨터통신 (01)");
        lecuterName.add("프로그래밍언어론 (03)");
        lecuterName.add("");
        lecuterName.add("");
        lecuterName.add("");
        lecuterName.add("");
        lecuterName.add("");
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPortalId() {
        return portalId;
    }

    public void setPortalId(String portalId) {
        this.portalId = portalId;
    }

    public boolean getChecked(int boardIndex) {
        return boardCheck.get(boardIndex);
    }

    public void setChecked(int boardIndex, boolean isChecked) {
        boardCheck.put(boardIndex, isChecked);
    }

    public Integer getScheduledHour() {
        return scheduledHour;
    }

    public void setScheduledHour(Integer scheduledHour) {
        this.scheduledHour = scheduledHour;
    }

    public Integer getScheduledMinute() {
        return scheduledMinute;
    }

    public void setScheduledMinute(Integer scheduledMinute) {
        this.scheduledMinute = scheduledMinute;
    }

    public String getLectureName(int lectureIndex) {
        return lecuterName.get(lectureIndex);
    }

    public void getLectureName(int lectureIndex, String newName) {
        lecuterName.remove(lectureIndex);
        lecuterName.add(lectureIndex, newName);
    }
}
