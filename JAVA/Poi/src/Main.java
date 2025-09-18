import bumil.custom.excel.ExcelGenerator;
import bumil.custom.excel.node.ExcelHeaderNode;
import bumil.custom.excel.node.ExcelStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 엑셀 파일을 생성 및 파싱하는 유틸리티 클래스입니다.
 * 다양한 헤더 구조를 테스트하는 메인 클래스입니다.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        runCase1();
        runCase2();
        runCase3();
        runCase4();
        runCase5();
    }

    private static void runCase1() throws Exception {
        System.out.println("[CASE 1] 1단계 Flat 헤더");
        ExcelHeaderNode root = new ExcelHeaderNode("Flat Header", null, Arrays.asList(
                new ExcelHeaderNode("이름", "name", null, null),
                new ExcelHeaderNode("나이", "age", null, null),
                new ExcelHeaderNode("이메일", "email", null, null)
        ), null);

        List<UserDTO> users = buildTestData();
        runTest("test_flat.xlsx", root, users, 0);
    }

    private static void runCase2() throws Exception {
        System.out.println("[CASE 2] 2단계 그룹핑 헤더");
        ExcelHeaderNode root = new ExcelHeaderNode("고객 정보", null, Arrays.asList(
                new ExcelHeaderNode("기본 정보", null, Arrays.asList(
                        new ExcelHeaderNode("이름", "name", null, null),
                        new ExcelHeaderNode("나이", "age", null, null)
                ), null),
                new ExcelHeaderNode("이메일", "email", null, null)
        ), null);

        List<UserDTO> users = buildTestData();
        runTest("test_grouped.xlsx", root, users, 1);
    }

    private static void runCase3() throws Exception {
        System.out.println("[CASE 3] 3단계 다단계 헤더");
        ExcelHeaderNode root = new ExcelHeaderNode("고객/주소 정보", null, Arrays.asList(
                new ExcelHeaderNode("고객 기본", null, Arrays.asList(
                        new ExcelHeaderNode("이름", "name", null, null),
                        new ExcelHeaderNode("나이", "age", null, null)
                ), null),
                new ExcelHeaderNode("연락처", null, Arrays.asList(
                        new ExcelHeaderNode("이메일", "email", null, null)
                ), null)
        ), null);

        List<UserDTO> users = buildTestData();
        runTest("test_multilevel.xlsx", root, users, 1);
    }

    private static void runCase4() throws Exception {
        System.out.println("[CASE 4] 비대칭 헤더");
        ExcelHeaderNode root = new ExcelHeaderNode("고객 정보", null, Arrays.asList(
                new ExcelHeaderNode("이름", "name", null, null),
                new ExcelHeaderNode("연락처", null, Arrays.asList(
                        new ExcelHeaderNode("이메일", "email", null, null)
                ), null)
        ), null);

        List<UserDTO> users = buildTestData();
        runTest("test_asymmetric.xlsx", root, users, 0);
    }

    private static void runCase5() throws Exception {
        System.out.println("[CASE 5] 병합 없는 헤더");
        ExcelHeaderNode root = new ExcelHeaderNode("", null, Arrays.asList(
                new ExcelHeaderNode("이름", "name", null, null),
                new ExcelHeaderNode("나이", "age", null, null),
                new ExcelHeaderNode("이메일", "email", null, null)
        ), null);

        List<UserDTO> users = buildTestData();
        runTest("test_no_merge.xlsx", root, users, 0);
    }

    private static void runTest(String fileName, ExcelHeaderNode root, List<UserDTO> users, int headerEndRow) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        // 엑셀 생성
        ExcelGenerator.generateExcel(new FileOutputStream(fileName), root, users, new HashMap<>());

        // 자동 칼럼 너비 조정 추가
        try (InputStream fis = new FileInputStream(fileName)) {
            Workbook tempWorkbook = new XSSFWorkbook(fis);
            Sheet tempSheet = tempWorkbook.getSheetAt(0);
            if (tempSheet.getRow(headerEndRow) != null) {
                int numberOfColumns = tempSheet.getRow(headerEndRow).getPhysicalNumberOfCells();
                for (int i = 0; i < numberOfColumns; i++) {
                    tempSheet.autoSizeColumn(i);
                }
            }
            try (OutputStream out = new FileOutputStream(fileName)) {
                tempWorkbook.write(out);
            }
            tempWorkbook.close();
        }

        // 파싱
        List<UserDTO> importedUsers;
        try (InputStream fis = new FileInputStream(fileName)) {
            importedUsers = ExcelGenerator.parseExcelToDto(fis, UserDTO.class, headerEndRow, root);
        }

        for (UserDTO user : importedUsers) {
            System.out.println(user.getName() + ", " + user.getAge() + ", " + user.getEmail());
        }
        System.out.println();
    }

    private static List<UserDTO> buildTestData() {
        return Arrays.asList(
                createUser("홍길동", 30, "hong@gmail.com"),
                createUser("김철수", 25, "kim@naver.com"),
                createUser("이영희", 22, "lee@daum.net")
        );
    }

    private static UserDTO createUser(String name, Integer age, String email) {
        UserDTO user = new UserDTO();
        user.setName(name);
        user.setAge(age);
        user.setEmail(email);
        return user;
    }

    public static class UserDTO {
        private String name;
        private Integer age;
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
