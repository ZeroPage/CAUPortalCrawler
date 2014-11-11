package test.apple.lemon.cauportalcrawlertest.model;

/**
 * Created by rino0601 on 2014. 11. 10..
 */
public class EClassContent {
    // todo, 등록일이 글을 수정하면 바뀐다고 한다. 이걸 통해 글수정을 감지 할 수도 있으니 들고 있어야 할지도...
    // todo, 위의 기능을 구현 할 거면 HashMap으로...
    private int lecture;
    private int board;
    private int itemIndex;
    private String title;
    private boolean isAlreadyRead = false;

    public EClassContent() {
        // orm lite
    }


    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
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
}
