package test.apple.lemon.cauportalcrawlertest.jsoupaser;

import java.util.List;

import test.apple.lemon.cauportalcrawlertest.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 10..
 */
public class LectureContentsParser extends CAUParser {
    public static final int BOARD_INDEX = 1;

    @Override
    public List<EClassContent> parse(String data) throws CAUParseException {
        throw new CAUParseException("unsupported board");
    }
}
