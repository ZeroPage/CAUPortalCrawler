package lemon.apple.caution.jsoupaser;

import org.jsoup.select.Elements;

import java.util.List;

import lemon.apple.caution.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 10..
 */
public class LectureContentsParser extends CAUParser {
    public static final int BOARD_INDEX = 1;

    @Override
    public List<EClassContent> parse(String data) throws CAUParseException {
        throw new CAUParseException("unsupported board");
    }

    @Override
    protected void setMetaData(EClassContent content, Elements nobr) {
        // ignored.. not use.
    }

    @Override
    protected int getBoardIndex() {
        return BOARD_INDEX;
    }
}
