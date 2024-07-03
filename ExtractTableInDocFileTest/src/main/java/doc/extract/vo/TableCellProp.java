package doc.extract.vo;

import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableCellProp {
    int rowSpan;
    int colSpan;
    String content;
}
