package test.apple.lemon.cauportalcrawlertest.jsoupaser;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import test.apple.lemon.cauportalcrawlertest.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 11..
 */
public class QnAParser extends CAUParser {
    public static final int BOARD_INDEX = 5;
    private static final String REG_WHO = "registerWho";
    private static final String REG_DATE = "registerDate";

    @Override
    protected void setMetaData(EClassContent content, Elements nobr) {
        Element fromWho = nobr.get(3);
        Element registerDate = nobr.get(4);
        content.put(REG_WHO, fromWho.text());
        content.put(REG_DATE, registerDate.text());
    }

    @Override
    protected int getBoardIndex() {
        return BOARD_INDEX;
    }
}
