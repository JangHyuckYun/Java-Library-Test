package doc.extract;

import doc.extract.util.MakeHtmlUtil;
import doc.extract.vo.TableCellProp;
import doc.extract.vo.TableDataVo;
import doc.extract.vo.TableProp;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractDocTable {
    
    public static List<TableDataVo> extract(String inputFilePath, boolean isTest) {
        try (FileInputStream fis = new FileInputStream(inputFilePath)) {
            HWPFDocument document = new HWPFDocument(fis);
            Range range = document.getRange();
            List<TableDataVo> result = new ArrayList<>();
            for (int i = 0; i < range.numParagraphs(); i++) {
                Paragraph paragraph = range.getParagraph(i);
                if (paragraph.isInTable()) {
                    MakeHtmlUtil makeHtmlUtil = new MakeHtmlUtil();
                    Table table = range.getTable(paragraph);
                    refineTableToHtml(table, makeHtmlUtil);
                    
                    // 이미 처리된 행 건너뛰기
                    i += table.numParagraphs() - 1;
                    System.out.println("==================");
                    System.out.println("html: "+makeHtmlUtil.toHtml(TableProp.builder().border(1).build()));
                    System.out.println();
                    result.add(TableDataVo.builder().rawContent(makeHtmlUtil.toHtml(TableProp.builder().border(1).build())).build());
                }
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void refineTableToHtml(Table table, MakeHtmlUtil makeHtmlUtil) {
        int numRows = table.numRows();
        for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
            TableRow row = table.getRow(rowIndex);
            int numCells = row.numCells();
            System.out.println("==============");
            System.out.println("numCells: "+numCells);
            for (int cellIndex = 0; cellIndex < numCells; cellIndex++) {
                TableCell cell = row.getCell(cellIndex);
                String cellText = getCellText(cell);
                System.out.println("currentText: "+cellText);

                int rowSpan = 1;
                int colSpan = 1;

                System.out.println("cell.toString(): "+cell.getDescriptor().toString());
                
                makeHtmlUtil.addRow(TableCellProp.builder().rowSpan(rowSpan).colSpan(colSpan).content(cellText).build());
            }
            makeHtmlUtil.submitRow();
        }
    }

    private static String getCellText(TableCell cell) {
        StringBuilder cellText = new StringBuilder();
        int numParagraphs = cell.numParagraphs();
        for (int i = 0; i < numParagraphs; i++) {
            Paragraph paragraph = cell.getParagraph(i);
            cellText.append(paragraph.text().trim()).append("\n");
        }
        return cellText.toString().trim();
    }
}
