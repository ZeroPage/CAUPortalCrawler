package lemon.apple.caution.jsoupaser;

import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import lemon.apple.caution.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 10..
 */
public abstract class CAUParser {
    public static int MAX_BOARD_BOUND = 6;

    public List<EClassContent> parse(String html) throws CAUParseException {
        ArrayList<EClassContent> list = new ArrayList<EClassContent>();

        Document parse = Jsoup.parse(html);
        Elements select = parse.select("tr.grid_body_row");
        if (select.size() != 11) {
            throw new CAUParseException("HTML has Special Characters like sharp (#)");
        }
        select = select.not(".w2grid_hidedRow");
        for (Element element : select) {
            Elements nobr = element.select("nobr");
            Element itemIndex = nobr.get(0);
            Element title = nobr.get(1);

            EClassContent content = new EClassContent();
            content.setIndex(Integer.parseInt(itemIndex.text()));
            content.setTitle(title.text());
            content.setBoard(getBoardIndex());
            content.setDatetime(new DateTime().toDate());
            setMetaData(content, nobr);
            list.add(content);
        }

        return list;
    }

    protected abstract void setMetaData(EClassContent content, Elements nobr);

    protected abstract int getBoardIndex();

}
