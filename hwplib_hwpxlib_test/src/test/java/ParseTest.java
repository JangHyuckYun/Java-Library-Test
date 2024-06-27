import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.object.bodytext.Section;
import kr.dogfoot.hwplib.object.bodytext.control.ControlTable;
import kr.dogfoot.hwplib.object.bodytext.control.table.Cell;
import kr.dogfoot.hwplib.object.bodytext.control.table.Row;
import kr.dogfoot.hwplib.object.bodytext.paragraph.Paragraph;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.object.content.section_xml.SectionXMLFile;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.Table;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.table.CellSpan;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.table.Tc;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.table.Tr;
import kr.dogfoot.hwpxlib.reader.HWPXReader;
import kr.dogfoot.hwpxlib.tool.textextractor.TextExtractor;
import kr.dogfoot.hwpxlib.tool.textextractor.TextMarks;
import org.hwp_hwpx.vo.TableDataVo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Disabled
public class ParseTest {

    private void saveToSampleHwpResults(String saveFileName, String tablesString) throws Exception{
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

    public static InputStream extractFileFromZip(String zipFilePath, String fileName) throws IOException, IOException {
        FileInputStream fileInputStream = new FileInputStream(zipFilePath);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        ZipEntry zipEntry;

        System.out.println("zipInputStream.getNextEntry().getName(): "+zipInputStream.getNextEntry().getName());
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.getName().equals(fileName)) {
                return zipInputStream;
            }
        }

        throw new IOException("File " + fileName + " not found in zip archive");
    }
    @Test
    void parseHwpxTest() throws Exception {
        //given
        String path = "./src/test/resources/sampleHwpFiles/";
        String fileName = "fail_hwpx_file";
        String extension = "hwpx";
        String filePath = path + fileName + "." + extension;
        
        // when
        File file = new File(filePath);
        
        HWPXFile hwpFile = HWPXReader.fromFile(file);
        List<TableDataVo> result = new ArrayList<>();
        for(SectionXMLFile sectionXMLFile : hwpFile.sectionXMLFileList().items()) {
            // hwpxlib 예제에 따르면 테이블 데이터 추출하기 위해서는 para -> run 객체 -> runItem 객체를 Table로 캐스팅하는것으로 나와 있어서 그대로 하였음.
            sectionXMLFile.paras().forEach(para -> {
                para.runs().forEach(run -> {
                    run.runItems().forEach(item -> {
                        try {
                            Table table = (Table) item;
                            StringBuffer htmlBuffer = new StringBuffer();
                            htmlBuffer.append("<table border='1'><tbody>");
                            if(table != null) {
                                for(Tr tr : table.trs()) {
                                    htmlBuffer.append("<tr>");
                                    for(Tc tc : tr.tcs()) {
                                        CellSpan cellSpan = tc.cellSpan();
                                        int colSpan = cellSpan.colSpan().intValue();
                                        int rowSpan = cellSpan.rowSpan().intValue();

                                        kr.dogfoot.hwpxlib.tool.textextractor.TextExtractMethod method = kr.dogfoot.hwpxlib.tool.textextractor.TextExtractMethod.AppendControlTextAfterParagraphText;
                                        TextMarks marks = new TextMarks().lineBreakAnd("\n").paraSeparatorAnd("\n");
                                        String text = TextExtractor.extractFrom(tc, method, marks);

                                        // 셀 병합 처리
                                        htmlBuffer.append("<td");
                                        if (rowSpan > 1) {
                                            htmlBuffer.append(" rowspan='").append(rowSpan).append("'");
                                        }
                                        if (colSpan > 1) {
                                            htmlBuffer.append(" colspan='").append(colSpan).append("'");
                                        }
                                        htmlBuffer.append(">").append(text).append("</td>");
                                    }
                                    htmlBuffer.append("</tr>");
                                }
                            }
                            htmlBuffer.append("</tbody></table>");
                            result.add(TableDataVo.builder().rawContent(htmlBuffer.toString()).build());
                        } catch (Exception e) {
                            // Table로 캐스팅 하는 도중 나타나는 에러는, 테이블 데이터가 아니므로 무시.
                            e.printStackTrace();
                        }
                    });
                });
            });
        }
        
        String testResult = result.stream().map(TableDataVo::getRawContent).collect(Collectors.joining(""));
        this.saveToSampleHwpResults(fileName+"_"+extension+"_result.html", testResult);

        // then...
    }


    @Test
    void parseHwpTest() throws Exception {
        //given
        String path = "./src/test/resources/sampleHwpFiles/";
        String fileName = "입법예고_일부개정조례안";
        String extension = "hwpx";
        String filePath = path + fileName + "." + extension;

        InputStream inputStream = extractFileFromZip(filePath, fileName + "." + extension);
        HWPFile hwpFile = HWPReader.fromInputStream(inputStream);
//        HWPFile hwpFile = HWPReader.fromFile(filePath);
        List<TableDataVo> dataVoList = new ArrayList<>();
        // HWP 파일 내의 모든 테이블을 탐색
        for (Section section : hwpFile.getBodyText().getSectionList()) {
            for (Paragraph paragraph : section.getParagraphs()) {
                if (paragraph.getControlList() != null) {
                    for (Object control : paragraph.getControlList()) {
                        if (control instanceof ControlTable) {
                            ControlTable table = (ControlTable) control;
                            String tableString = convertTableToHTML(table);
                            dataVoList.add(TableDataVo.builder().rawContent(tableString).build());
                        }
                    }
                }
            }
        }

        String result = dataVoList.stream().map(TableDataVo::getRawContent).collect(Collectors.joining(","));
        this.saveToSampleHwpResults(fileName +"_"+extension+"_result.html", result);
    }

    private String convertTableToHTML(ControlTable table) {
        StringBuilder html = new StringBuilder("<table border='1'>");
        for (Row row : table.getRowList()) {
            html.append("<tr>");
            for (Cell cell : row.getCellList()) {
                int rowSpan = cell.getListHeader().getRowSpan();
                int colSpan = cell.getListHeader().getColSpan();
                String cellText = extractCellText(cell);
                html.append("<td");
                if (rowSpan > 1) {
                    html.append(" rowspan='").append(rowSpan).append("'");
                }
                if (colSpan > 1) {
                    html.append(" colspan='").append(colSpan).append("'");
                }
                html.append(">").append(cellText.replace("\n", "<br>")).append("</td>");

            }
            html.append("</tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    private String extractCellText(Cell cell) {
        StringBuilder cellText = new StringBuilder();
        for (Paragraph para : cell.getParagraphList()) {
            if (para.getText() != null) {
                for (int i = 0; i < para.getText().getCharList().size(); i++) {
                    String c = convertHWPCharToString(para.getText().getCharList().get(i).getCode());
//					System.out.println("c: "+c + " / " + "code: "+para.getText().getCharList().get(i).getCode());
                    cellText.append(c);
                }
            }
        }
        return cellText.toString();
    }

    private String convertHWPCharToString(int charCode) {
        return Character.toString((char) charCode);
    }
}
