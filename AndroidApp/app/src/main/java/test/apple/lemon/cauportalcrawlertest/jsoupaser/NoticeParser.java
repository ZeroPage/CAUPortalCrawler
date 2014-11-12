package test.apple.lemon.cauportalcrawlertest.jsoupaser;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import test.apple.lemon.cauportalcrawlertest.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 10..
 */
public class NoticeParser extends CAUParser {

    public static final int BOARD_INDEX = 0;
    private static final String REG_DATE = "registerDate";

    @Override
    protected void setMetaData(EClassContent content, Elements nobr) {
        Element registerDate = nobr.get(4);
        content.put(REG_DATE, registerDate.text());
    }

    @Override
    protected int getBoardIndex() {
        return BOARD_INDEX;
    }
}
