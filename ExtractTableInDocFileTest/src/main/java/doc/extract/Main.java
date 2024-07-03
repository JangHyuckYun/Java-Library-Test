package doc.extract;

import doc.extract.util.FileUtil;
import doc.extract.vo.TableDataVo;

import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        try {
            // 실제 추출 로직
            String fileName = "testtest.doc";
            String inputFilePath = "./src/main/resources/sampleDocFiles/"+fileName;
            List<TableDataVo> list = ExtractDocTable.extract(inputFilePath, true);

            // 테이블이 정상적으로 파싱되었나 확인하기 위해 파일에 저장.
            String stringForSaveHtmlFile = list.stream().map(item -> item.getRawContent()).collect(Collectors.joining("</br></br>"));
            String saveFileName = fileName+".html";
            FileUtil.saveToSampleHwpResults(saveFileName, stringForSaveHtmlFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}