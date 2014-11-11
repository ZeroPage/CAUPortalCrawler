package test.apple.lemon.cauportalcrawlertest.jsoupaser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import test.apple.lemon.cauportalcrawlertest.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 10..
 */
public class NoticeParser extends CAUParser {

    public static final int BOARD_INDEX = 0;

    @Override
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
            //Element updateDate = nobr.get(4);

            EClassContent content = new EClassContent();
            content.setItemIndex(Integer.parseInt(itemIndex.text()));
            content.setTitle(title.text());
            content.setBoard(BOARD_INDEX);
            list.add(content);
        }

        return list;
    }
}
