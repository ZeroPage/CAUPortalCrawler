package test.apple.lemon.cauportalcrawlertest.model;

import java.util.ArrayList;

/**
 * Created by rino0601 on 2014. 11. 11..
 */
public class TempSC extends ArrayList<EClassContent> {
    private static TempSC instance = new TempSC();

    private TempSC() {
    }

    public static TempSC getInstance() {
        return instance;
    }

}
