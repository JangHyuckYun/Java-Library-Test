package org.hwp_hwpx;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.hwp_hwpx.util.FileUtil;
import org.hwp_hwpx.util.MakeHtmlUtil;
import org.hwp_hwpx.vo.TableCellProp;
import org.hwp_hwpx.vo.TableProp;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocxParseTest {
    public static void main(String[] args) {
        String fileName = "POI_collection_data_description_2.docx";
        String docxFilePath = "./src/main/resources/sampleDocxFiles/sample2.doc"; // docx 파일 경로
        File file = new File(docxFilePath);
        System.out.println("file: exist:" +file.exists());
        extractTableData(docxFilePath, fileName);
    }
    public static void extractTableData(String docxFilePath, String fileName) {
        try {
            FileInputStream fis = new FileInputStream(docxFilePath);
            XWPFDocument document = new XWPFDocument(fis);

            // 문서에서 모든 테이블 가져오기
            List<XWPFTable> tables = document.getTables();
            List<String> tableStringList = new ArrayList<>();
            for (XWPFTable table : tables) {
                MakeHtmlUtil makeHtmlUtil = new MakeHtmlUtil();
                Map<Integer, Integer> rowSpanMap = new HashMap<>();
                // 각 테이블의 행 가져오기
                List<XWPFTableRow> rows = table.getRows();
                int rowIndex = 0;
                for (XWPFTableRow row : rows) {
                    // 각 행의 셀 가져오기
                    int cellIndex = 0;
                    for(XWPFTableCell cellItem : row.getTableCells()) {
                        // 병합된 row 행이 존재하고, 해당 값이 1 이상이면 추가로 컬럼 생성되지 않도록 조정
                        if (rowSpanMap.containsKey(cellIndex) && rowSpanMap.get(cellIndex) > 0) {
                            rowSpanMap.put(cellIndex, rowSpanMap.get(cellIndex) - 1);
                            cellIndex++;
                            continue;
                        }
                        
                        CTTc ctTc = cellItem.getCTTc();
                        CTTcPr tcPr = ctTc.getTcPr();
                        int colSpan = 1;
                        if (tcPr != null && tcPr.isSetGridSpan()) {
                            colSpan = tcPr.getGridSpan().getVal().intValue();
                        }
                        
                        int rowSpan = 1;
                        // 병합된 경우에만 row의 병합된 값 조회
                        if (tcPr != null && tcPr.isSetVMerge()) {
                            CTVMerge vMerge = tcPr.getVMerge();
                            if (vMerge.getVal() == STMerge.RESTART || vMerge.getVal() == null) {
                                rowSpan = calculateRowSpan(rows, rowIndex, cellIndex);
                                // 예를 들어 3개의 row가 병합되었으면 2개의 셀만 생성이 안 되도록 막기 위해, -1 된 상태로 저장.
                                rowSpanMap.put(cellIndex, rowSpan - 1); 
                            }
                        }
                        
                        makeHtmlUtil.addRow(TableCellProp.builder()
                                .rowSpan(rowSpan)
                                .colSpan(colSpan)
                                .content(cellItem.getText())
                                .build());
                        cellIndex++;
                    }
                    
                    makeHtmlUtil.submitRow();
                    rowIndex++;
                }

                String tableString = makeHtmlUtil.makeTable(TableProp.builder().border(1).build());
                System.out.println("table: "+tableString);
                tableStringList.add(tableString);
            }
            FileUtil.saveToSampleHwpResults(fileName+".html", tableStringList.stream().collect(Collectors.joining("")));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static int calculateRowSpan(List<XWPFTableRow> rows, int startRow, int cellIndex) {
        int rowSpan = 1;
        System.out.println();
        for (int i = startRow + 1; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            XWPFTableCell cell = row.getTableCells().get(cellIndex);
            CTTcPr tcPr = cell.getCTTc().getTcPr();
            if (tcPr != null && tcPr.isSetVMerge() && (tcPr.getVMerge().getVal() == STMerge.CONTINUE || tcPr.getVMerge().getVal() == null)) {
                rowSpan++;
            } else {
                break;
            }
        }
        return rowSpan;
    }
}
