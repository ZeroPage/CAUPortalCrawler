package test.apple.lemon.cauportalcrawlertest.jsoupaser;

/**
 * Created by rino0601 on 2014. 11. 10..
 */
public class ParserFactory {
    public static CAUParser createParser(int boardIndex) {
        switch (boardIndex) {
            case NoticeParser.BOARD_INDEX: // 공지사항
                return new NoticeParser();
            case LectureContentsParser.BOARD_INDEX:
                return new LectureContentsParser();
            case HomeworkParser.BOARD_INDEX:
                return new HomeworkParser();
            case TeamProjectParser.BOARD_INDEX:
                return new TeamProjectParser();
            case SharedDataParser.BOARD_INDEX:
                return new SharedDataParser();
            case QnAParser.BOARD_INDEX:
                return new QnAParser();
        }
        throw new IllegalArgumentException("unsupported board occur");
    }
}
