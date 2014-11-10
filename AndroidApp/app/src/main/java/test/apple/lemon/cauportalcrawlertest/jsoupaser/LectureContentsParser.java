package test.apple.lemon.cauportalcrawlertest.jsoupaser;

import java.util.ArrayList;
import java.util.List;

import test.apple.lemon.cauportalcrawlertest.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 10..
 */
public class LectureContentsParser extends Parser {
    public static final int BOARD_INDEX = 1;

    @Override
    public List<EClassContent> parse(String data) {
        ArrayList<EClassContent> list = new ArrayList<EClassContent>();
        // todo, 강의 컨텐츠 가진 과목 듣는사람 찾아내서 샘플 데이터 얻어올 것.
        return list;
    }
}
