package lemon.apple.caution.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rino0601 on 2014. 11. 10..
 */
public class EClassContent {
    public static final String LECTURE_FIELD = "lecture";
    public static final String BOARD_FIELD = "board";
    public static final String INDEX_FIELD = "index";
    public static final String DATETIME_FIELD = "datetime";
    public static final String ALREADY_READ_FIELD = "isAlreadyRead";

    @DatabaseField(generatedId = true)
    private Integer id; // id 를 활용할 방법이 생각이 나질 않아서 아쉽다 ㅠㅜ
    @DatabaseField(columnName = LECTURE_FIELD)
    private Integer lecture;
    @DatabaseField(columnName = BOARD_FIELD)
    private Integer board;
    @DatabaseField(columnName = INDEX_FIELD)
    private Integer index;
    @DatabaseField
    private String title;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private HashMap<String, String> meta = new HashMap<String, String>();

    @DatabaseField(columnName = ALREADY_READ_FIELD)
    private Boolean isAlreadyRead = false;
    @DatabaseField(columnName = DATETIME_FIELD, dataType = DataType.DATE)
    private Date datetime; // for sort.

    public EClassContent() {
        // orm lite
    }

    public static Map<String, Object> queryMap(Integer lecture, Integer board, Integer itemIndex) {
        HashMap<String, Object> forQuery = new HashMap<String, Object>();
        forQuery.put(LECTURE_FIELD, lecture);
        forQuery.put(BOARD_FIELD, board);
        forQuery.put(INDEX_FIELD, itemIndex);
        return forQuery;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLecture() {
        return lecture;
    }

    public void setLecture(int lecture) {
        this.lecture = lecture;
    }

    public int getBoard() {
        return board;
    }

    public void setBoard(int board) {
        this.board = board;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAlreadyRead() {
        return isAlreadyRead;
    }

    public void setAlreadyRead(boolean isAleadyRead) {
        this.isAlreadyRead = isAleadyRead;
    }

    public String put(String key, String value) {
        return meta.put(key, value);
    }

    public String get(Object key) {
        return meta.get(key);
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EClassContent
                && lecture.equals(((EClassContent) o).lecture)
                && board.equals(((EClassContent) o).board)
                && index.equals(((EClassContent) o).index)
                && title.equals(((EClassContent) o).title)
                && meta.equals(((EClassContent) o).meta);
    }
}
