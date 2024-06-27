# HWP / HWPX 파일에서 테이블 데이터를 추출하여, HTML로 변환

## 이슈사항
- Library -> hwpxlib
  - Table Cell 안의 데이터는 텍스트라고 가정하고 추출하게 되어있음.
    - Sub로 테이블이 있는 경우에도 텍스트만 추출되는 문제가 있음. 