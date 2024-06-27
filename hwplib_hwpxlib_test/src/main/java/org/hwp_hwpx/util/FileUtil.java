package org.hwp_hwpx.util;

import java.io.File;
import java.io.FileWriter;

public class FileUtil {
    public static void saveToSampleHwpResults(String saveFileName, String tablesString) throws Exception{
        File file = new File("./src/test/resources/sampleHwpResults/"+saveFileName);
        FileWriter fw = new FileWriter(file, false);

        StringBuffer buffer = new StringBuffer();
        buffer.append("<!doctype html>\n" +
                        "<html lang=\"ko\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\"\n" +
                        "          content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">\n" +
                        "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">\n" +
                        "    <title>Document</title>\n" +
                        "</head>\n" +
                        "<body>")
                .append(tablesString)
                .append("</body></html>");

        fw.write(buffer.toString());
        fw.flush();
        fw.close();
    }
}
