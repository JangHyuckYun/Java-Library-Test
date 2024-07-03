# .DOC 파일에서 테이블 데이터 추출

## 개요
DOC 파일의 데이터 중 테이블만 추출하여 저장하는 로직이 필요하여, 테스트하기 위해 프로젝트 생성.

## 문제점
- (240703) 테이블 추출 로직은 문제가 없는데, 병합된 셀의 경우에 대한 인식이 애매하다. 
  Apache POI 라이브러리의 경우, 각 셀마다 rowSpan, colSpan의 정보를 가지고 있지 않고, 
  "isMerged"라고 병합되었는지 판별하는 메서드가 있는데, 병합을 했음에도 전부 false가 나와버려, 일단 보류 상태이다.  
    
  (일단 GPT가 데이터를 인식하는데는 문제가 없을거라 보고 넘기고, 나중에 문제에 대한 원인을 찾아야할 것 같다. (DOCX 파일에서는 인식이 잘 되었었음.))

## Sample 데이터 출처
- https://sample-videos.com/download-sample-doc-file.php
- https://file-examples.com/index.php/sample-documents-download/sample-doc-download/