package test.apple.lemon.cauportalcrawlertest.jsoupaser;

import org.jsoup.select.Elements;

import java.util.List;

import test.apple.lemon.cauportalcrawlertest.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 11..
 */
public class TeamProjectParser extends CAUParser {
    public static final int BOARD_INDEX = 3;

    @Override
    public List<EClassContent> parse(String data) throws CAUParseException {
        throw new CAUParseException("unsupported board");
    }

    @Override
    protected void setMetaData(EClassContent content, Elements nobr) {
        // not use. ignore impl
    }

    @Override
    protected int getBoardIndex() {
        return BOARD_INDEX;
    }
}
