package bumil.custom.excel;

import bumil.custom.excel.node.ExcelHeaderNode;
import bumil.custom.excel.node.ExcelStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 엑셀 파일을 생성 및 파싱하는 유틸리티 클래스입니다.
 * <p>다단계 헤더, 1차/다차 계층 병합, 스타일 지정 기능을 포함합니다.</p>
 */
public class ExcelGenerator {

    private static int maxDepth = 1; // 트리 최대 깊이

    /**
     * 엑셀 파일을 생성합니다.
     * @param out 출력 스트림
     * @param headerRoot 헤더 트리 루트
     * @param dataList 데이터 리스트
     * @param bodyStyleMap 필드별 바디 스타일 맵
     * @throws Exception 생성 실패 시
     */
    public static void generateExcel(OutputStream out, ExcelHeaderNode headerRoot, List<?> dataList, Map<String, ExcelStyle> bodyStyleMap) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");
        Map<Integer, Row> rowMap = new HashMap<>();

        maxDepth = calculateMaxDepth(headerRoot);
        renderHeader(sheet, headerRoot, 0, 0, rowMap, workbook);
        List<ExcelHeaderNode> leafNodes = collectLeafNodes(headerRoot);

        int rowIdx = maxDepth;
        for (Object dto : dataList) {
            Row row = sheet.createRow(rowIdx++);
            int colIdx = 0;
            for (ExcelHeaderNode leaf : leafNodes) {
                Cell cell = row.createCell(colIdx++);
                Object value = getFieldValue(dto, leaf.getFieldName());
                if (value != null) {
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
                ExcelStyle bodyStyle = bodyStyleMap.getOrDefault(leaf.getFieldName(), defaultBodyStyle(value));
                cell.setCellStyle(createCellStyle(workbook, bodyStyle));
            }
        }
        int numberOfColumns = collectLeafNodes(headerRoot).size();
        for (int i = 0; i < numberOfColumns; i++) {
            sheet.autoSizeColumn(i);
        }
        workbook.write(out);
        workbook.close();
    }
    private static void collectLeafNodesRecursive(ExcelHeaderNode node, List<ExcelHeaderNode> result) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            result.add(node);
        } else {
            for (ExcelHeaderNode child : node.getChildren()) {
                collectLeafNodesRecursive(child, result);
            }
        }
    }
    private static int calculateMaxDepth(ExcelHeaderNode node) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) return 1;
        int childDepth = node.getChildren().stream()
                .mapToInt(ExcelGenerator::calculateMaxDepth)
                .max()
                .orElse(0);
        return 1 + childDepth;
    }

    private static int renderHeader(Sheet sheet, ExcelHeaderNode node, int rowIdx, int colIdx, Map<Integer, Row> rowMap, Workbook workbook) {
        Row row = rowMap.computeIfAbsent(rowIdx, sheet::createRow);
        Cell cell = row.createCell(colIdx);
        cell.setCellValue(node.getTitle() != null ? node.getTitle() : "");
        cell.setCellStyle(createCellStyle(workbook, node.getStyle()));

        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            if (rowIdx < maxDepth - 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowIdx, maxDepth - 1, colIdx, colIdx));
            }
            return 1;
        }

        int width = 0;
        for (ExcelHeaderNode child : node.getChildren()) {
            width += renderHeader(sheet, child, rowIdx + 1, colIdx + width, rowMap, workbook);
        }

        if (width > 1) {
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, colIdx, colIdx + width - 1));
        }
        return width;
    }

    private static List<ExcelHeaderNode> collectLeafNodes(ExcelHeaderNode node) {
        List<ExcelHeaderNode> leaves = new ArrayList<>();
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            leaves.add(node);
        } else {
            for (ExcelHeaderNode child : node.getChildren()) {
                leaves.addAll(collectLeafNodes(child));
            }
        }
        return leaves;
    }

    private static Object getFieldValue(Object dto, String fieldName) throws Exception {
        Field field = dto.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(dto);
    }

    private static CellStyle createCellStyle(Workbook workbook, ExcelStyle excelStyle) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        if (excelStyle != null) {
            if (excelStyle.getFontName() != null) font.setFontName(excelStyle.getFontName());
            if (excelStyle.getFontSize() > 0) font.setFontHeightInPoints(excelStyle.getFontSize());
            font.setBold(excelStyle.isBold());
            style.setFont(font);
            if (excelStyle.getBackgroundColor() != null) {
                style.setFillForegroundColor(excelStyle.getBackgroundColor().getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
            if (excelStyle.getAlignment() != null) style.setAlignment(excelStyle.getAlignment());
            if (excelStyle.getVerticalAlignment() != null) style.setVerticalAlignment(excelStyle.getVerticalAlignment());
        }
        return style;
    }

    private static ExcelStyle defaultBodyStyle(Object value) {
        ExcelStyle style = new ExcelStyle();
        style.setFontName("맑은 고딕");
        style.setFontSize((short) 10);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(value instanceof Number ? HorizontalAlignment.RIGHT : HorizontalAlignment.CENTER);
        return style;
    }

    // --- 파싱 관련 메소드 그대로 유지 ---

    public static <T> List<T> parseExcelToDto(InputStream in, Class<T> dtoClass, int headerEndRow, ExcelHeaderNode headerRoot) throws Exception {
        List<T> result = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(in);
        Sheet sheet = workbook.getSheetAt(0);

        List<ExcelHeaderNode> leafNodes = collectLeafNodes(headerRoot);

        for (int i = headerEndRow + 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isEmptyRow(row)) continue;

            T dto = dtoClass.getDeclaredConstructor().newInstance();
            for (int j = 0; j < leafNodes.size(); j++) {
                Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                Object value = null;
                switch (cell.getCellType()) {
                    case STRING: value = cell.getStringCellValue(); break;
                    case NUMERIC: value = cell.getNumericCellValue(); break;
                    case BOOLEAN: value = cell.getBooleanCellValue(); break;
                    default: break;
                }
                setFieldValue(dto, leafNodes.get(j).getFieldName(), value);
            }
            result.add(dto);
        }

        workbook.close();
        return result;
    }


    private static Cell getMergedCellIfBlank(Sheet sheet, Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            if (cell == null) return null;
            int rowIdx = cell.getRowIndex();
            int colIdx = cell.getColumnIndex();
            for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                CellRangeAddress range = sheet.getMergedRegion(i);
                if (range.isInRange(rowIdx, colIdx)) {
                    Row topRow = sheet.getRow(range.getFirstRow());
                    if (topRow != null) {
                        return topRow.getCell(range.getFirstColumn(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    }
                }
            }
        }
        return cell;
    }

    private static boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, String> buildTitleToFieldNameMap(ExcelHeaderNode node) {
        Map<String, String> map = new HashMap<>();
        collectMapping(node, map);
        return map;
    }

    private static void collectMapping(ExcelHeaderNode node, Map<String, String> map) {
        if (node.getFieldName() != null) {
            map.put(node.getTitle(), node.getFieldName());
        }
        if (node.getChildren() != null) {
            for (ExcelHeaderNode child : node.getChildren()) {
                collectMapping(child, map);
            }
        }
    }

    private static void setFieldValue(Object dto, String fieldName, Object value) throws Exception {
        if (value == null) return;
        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        for (Method method : dto.getClass().getMethods()) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                Object convertedValue = convertValue(value, method.getParameterTypes()[0]);
                method.invoke(dto, convertedValue);
                return;
            }
        }
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;

        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Double) return ((Double) value).intValue();
            if (value instanceof String) {
                String str = ((String) value).trim();
                if (isParsableAsNumber(str)) {
                    return Integer.parseInt(str);
                } else {
                    return null; // 변환 불가 → 무시
                }
            }
        } else if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Double) return ((Double) value).longValue();
            if (value instanceof String) {
                String str = ((String) value).trim();
                if (isParsableAsNumber(str)) {
                    return Long.parseLong(str);
                } else {
                    return null;
                }
            }
        } else if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) return ((Number) value).doubleValue();
            if (value instanceof String) {
                String str = ((String) value).trim();
                if (isParsableAsNumber(str)) {
                    return Double.parseDouble(str);
                } else {
                    return null;
                }
            }
        } else if (targetType == String.class) {
            return value.toString();
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) return value;
            if (value instanceof String) return Boolean.parseBoolean(((String) value).trim());
        }
        return value;
    }
    private static boolean isParsableAsNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}