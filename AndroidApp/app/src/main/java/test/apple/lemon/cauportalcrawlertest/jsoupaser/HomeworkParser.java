package test.apple.lemon.cauportalcrawlertest.jsoupaser;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import test.apple.lemon.cauportalcrawlertest.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 11..
 */
public class HomeworkParser extends CAUParser {
    public static final int BOARD_INDEX = 2;
    public static final String DUE_DATE = "dueDate";
    public static final String EXTEND_DATE = "extendDate";
    public static final String STATUS = "status";

    @Override
    protected void setMetaData(EClassContent content, Elements nobr) {
        Element dueDate = nobr.get(2);
        Element extendDate = nobr.get(3);
        Element status = nobr.get(4);
        content.put(DUE_DATE, dueDate.text());
        content.put(EXTEND_DATE, extendDate.text());
        content.put(STATUS, status.text());
    }

    @Override
    protected int getBoardIndex() {
        return BOARD_INDEX;
    }
}
