package bumil.custom.excel.node;

import java.util.List;
/**
 * 엑셀 헤더 구조를 표현하는 클래스입니다.
 * <p>트리 형태로 다단계 헤더 구성이 가능합니다.</p>
 */
public class ExcelHeaderNode {
    String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public List<ExcelHeaderNode> getChildren() {
        return children;
    }

    public void setChildren(List<ExcelHeaderNode> children) {
        this.children = children;
    }

    public ExcelStyle getStyle() {
        return style;
    }

    public void setStyle(ExcelStyle style) {
        this.style = style;
    }

    String fieldName;
    List<ExcelHeaderNode> children;
    ExcelStyle style;

    public ExcelHeaderNode(String title, String fieldName, List<ExcelHeaderNode> children, ExcelStyle style) {
        this.title = title;
        this.fieldName = fieldName;
        this.children = children;
        this.style = style;
    }
}
