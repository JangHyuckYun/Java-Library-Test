package doc.extract.vo;

import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableProp {
    int border = -1;
    
    public String initBorder() {
        return border != -1 ? " border='"+border+"' " : "";
    }
}
