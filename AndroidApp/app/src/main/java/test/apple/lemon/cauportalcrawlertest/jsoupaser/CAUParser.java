package test.apple.lemon.cauportalcrawlertest.jsoupaser;

import java.util.List;

import test.apple.lemon.cauportalcrawlertest.model.EClassContent;

/**
 * Created by rino0601 on 2014. 11. 10..
 */
public abstract class CAUParser {
    public abstract List<EClassContent> parse(String data) throws CAUParseException;
}
