package org.hwp_hwpx.util;

import org.hwp_hwpx.vo.TableCellProp;
import org.hwp_hwpx.vo.TableProp;

import java.util.ArrayList;
import java.util.List;

public class MakeHtmlUtil {
    List<List<TableCellProp>> rows;
    List<TableCellProp> row;

    public MakeHtmlUtil() {
        this.rows = new ArrayList<>();
        this.row = new ArrayList<>();
    }

    public void addRows(List<TableCellProp> row) {
        this.rows.add(row);
    }
    public void addRow(TableCellProp cellProp) {
        this.row.add(cellProp);
    }

    public void empty() {
        this.rows = new ArrayList<>();
        this.row = new ArrayList<>();
    }

    public void emptyRow() {
        this.row = new ArrayList<>();
    }

    public void submitRow() {
        this.addRows(this.row);
        this.emptyRow();
    }

    public String makeTable(TableProp tableProp) {
        StringBuffer html = new StringBuffer();
        html.append("<table").append(tableProp.getBorder() > 0 ? " border='"+tableProp.getBorder()+"'" : "").append("><tbody>");
        this.rows.forEach(row -> {
            html.append("<tr>");
            row.forEach(tableCellProp -> {
                html.append("<td");
                if (tableCellProp.getRowSpan() > 1) {
                    html.append(" rowspan='").append(tableCellProp.getRowSpan()).append("'");
                }
                if (tableCellProp.getColSpan() > 1) {
                    html.append(" colspan='").append(tableCellProp.getColSpan()).append("'");
                }
                html.append(">").append(tableCellProp.getContent().replace("\n", "<br>")).append("</td>");
            });
            html.append("</tr>");
        });
        html.append("</tbody></table>");

        return html.toString();
    }

    public String makeTable() {
        return this.makeTable(TableProp.builder().build());
    }

    public String toHtml() {
        StringBuffer html = new StringBuffer();

        html.append("<html>");
        // html 파일로 따로 생성 후 띄우려고할 시 한글 깨짐 방지.
        html.append("<head><meta charset=\"UTF-8\"></head>");
        html.append("<body>");
        html.append(this.makeTable());
        html.append("</body></html>");

        return html.toString();
    }
}
