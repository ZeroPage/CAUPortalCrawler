package lemon.apple.caution.jsoupaser;

import android.test.AndroidTestCase;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.List;

import lemon.apple.caution.model.EClassContent;

public class NoticeParserTest extends AndroidTestCase {
    public void testParse() throws Exception {
        String testFiles[] = {
                "raw/table[0,0].html",
                "raw/table[1,0].html",
//                "raw/table[2,0].html", // it produce CAUException.
                "raw/table[3,0].html",
                "raw/table[4,0].html",
                "raw/table[5,0].html",
        };
        for (String file : testFiles) {
            InputStream open = getContext().getAssets().open(file);
            String android_val = IOUtils.toString(open);

            CAUParser parser = ParserFactory.createParser(0);
            List<EClassContent> contents = parser.parse(android_val);
            for (EClassContent content : contents) {
                content.setLecture(0);
            }

            assertNotSame(0, contents.size());

            EClassContent eClassContent = contents.get(0);
            assertEquals(0, eClassContent.getLecture());
            assertEquals(0, eClassContent.getBoard());
            String title = eClassContent.getTitle();
            boolean aleadyRead = eClassContent.isAlreadyRead();
        }
    }
}