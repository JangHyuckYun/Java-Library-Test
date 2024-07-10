package doc.extract;

import com.spire.doc.*;
import com.spire.doc.collections.*;
import doc.extract.util.MakeHtmlUtil;
import doc.extract.vo.TableCellProp;
import doc.extract.vo.TableDataVo;
import doc.extract.vo.TableProp;

import java.util.ArrayList;
import java.util.List;

public class ExtractDocTableUsingSpire {

    public static List<TableDataVo> extract(String inputFilePath) {
        List<TableDataVo> result = new ArrayList<>();
    
        Document document = new Document(inputFilePath);
        SectionCollection sectionCollection = document.getSections();
        int sectionCount = sectionCollection.getCount();
        for (int sectionIndex = 0; sectionIndex < sectionCount; sectionIndex++) {
            TableCollection tableCollection = sectionCollection.get(sectionIndex).getTables();
            int tableCount = tableCollection.getCount();

            for (int tableIndex = 0; tableIndex < tableCount; tableIndex++) {
                Table tb = tableCollection.get(tableIndex);
                MakeHtmlUtil makeHtmlUtil = new MakeHtmlUtil();
                RowCollection rowCollection = tb.getRows();
                System.out.println("==================================================");
                int rowCount = rowCollection.getCount();
                for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                    System.out.println("=========");
                    TableRow tableRow = rowCollection.get(rowIndex);
                    CellCollection cellCollection = tableRow.getCells();
                    int cellCount = cellCollection.getCount();
                    for (int cellIndex = 0; cellIndex < cellCount; cellIndex++) {
                        TableCell tableCell = cellCollection.get(cellIndex);
                        int colSpan = tableCell.getCellFormat().getHorizontalMerge().getValue();
                        int rowSpan = tableCell.getCellFormat().getVerticalMerge().getValue();
                        String content = getCellText(tableCell.getParagraphs());
                        TableCellProp cellProp = TableCellProp.builder().colSpan(colSpan).rowSpan(rowSpan).content(content).build();
                        makeHtmlUtil.addRow(cellProp);
                        System.out.print("cellProp: "+ cellProp.toString());
                        System.out.println(" / width:" +tableCell.getCellWidth());
                    }
                    makeHtmlUtil.submitRow();
                }
                String htmlContent = makeHtmlUtil.toHtml(TableProp.builder().border(1).build());
                System.out.println("html: "+htmlContent);
                result.add(TableDataVo.builder().rawContent(htmlContent).build());
            }   
        }
        
        return result;
    }
    
    private static String getCellText(ParagraphCollection paragraphCollection) {
        int count = paragraphCollection.getCount();
        StringBuffer stringBuffer = new StringBuffer();
        
        for(int i = 0; i < count; i++) {
            stringBuffer.append(paragraphCollection.get(i).getText());
        }
        
        return stringBuffer.toString();
    }
}
