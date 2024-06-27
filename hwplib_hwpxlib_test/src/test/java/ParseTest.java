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
import org.hwp_hwpx.util.MakeHtmlUtil;
import org.hwp_hwpx.vo.TableCellProp;
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

        fw.write(tablesString);
        fw.flush();
        fw.close();
    }

    public static InputStream extractFileFromZip(String zipFilePath, String fileName) throws IOException, IOException {
        FileInputStream fileInputStream = new FileInputStream(zipFilePath);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        ZipEntry zipEntry;

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
        for (SectionXMLFile sectionXMLFile : hwpFile.sectionXMLFileList().items()) {
            // hwpxlib 예제에 따르면 테이블 데이터 추출하기 위해서는 para -> run 객체 -> runItem 객체를 Table로 캐스팅하는것으로 나와 있어서 그대로 하였음.
            sectionXMLFile.paras().forEach(para -> {
                para.runs().forEach(run -> {
                    run.runItems().forEach(item -> {
                        try {
                            Table table = (Table) item;
                            MakeHtmlUtil makeHtmlUtil = new MakeHtmlUtil();
                            if (table != null) {
                                for (Tr tr : table.trs()) {
                                    for (Tc tc : tr.tcs()) {
                                        CellSpan cellSpan = tc.cellSpan();
                                        int colSpan = cellSpan.colSpan().intValue();
                                        int rowSpan = cellSpan.rowSpan().intValue();

                                        kr.dogfoot.hwpxlib.tool.textextractor.TextExtractMethod method = kr.dogfoot.hwpxlib.tool.textextractor.TextExtractMethod.AppendControlTextAfterParagraphText;
                                        TextMarks marks = new TextMarks().lineBreakAnd("\n").paraSeparatorAnd("\n");
                                        String text = TextExtractor.extractFrom(tc, method, marks);
                                        makeHtmlUtil.addRow(TableCellProp.builder()
                                                .colSpan(colSpan)
                                                .rowSpan(rowSpan)
                                                .content(text)
                                                .build());
                                    }
                                    makeHtmlUtil.submitRow();
                                }
                            }
                            result.add(TableDataVo.builder().rawContent(makeHtmlUtil.toHtml()).build());
                        } catch (ClassCastException e) {
                            // Table로 캐스팅 하는 도중 나타나는 에러는, 테이블 데이터가 아니므로 무시.
                        } catch (Exception e) {
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
        String fileName = "table_1";
        String extension = "hwp";
        String filePath = path + fileName + "." + extension;
        
        HWPFile hwpFile = HWPReader.fromFile(filePath);
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
        MakeHtmlUtil makeHtmlUtil = new MakeHtmlUtil();
        for (Row row : table.getRowList()) {
            for (Cell cell : row.getCellList()) {
                int rowSpan = cell.getListHeader().getRowSpan();
                int colSpan = cell.getListHeader().getColSpan();
                String cellText = extractCellText(cell);

                makeHtmlUtil.addRow(TableCellProp.builder()
                        .colSpan(colSpan)
                        .rowSpan(rowSpan)
                        .content(cellText)
                        .build());
            }
            makeHtmlUtil.submitRow();
        }

        return makeHtmlUtil.toHtml();
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
