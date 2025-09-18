

```markdown
# Custom POI Excel Generator

CustomPOI는 다양한 헤더 구조를 지원하는 엑셀 파일 생성 및 읽기 유틸리티입니다.

## 주요 기능
- 다단계/병합 셀을 포함하는 헤더 구조 지원
- DTO 기반으로 엑셀 바디 작성
- DTO 기반 엑셀 데이터 파싱
- **자동 열 너비 조정 (autoSizeColumn 적용)** ✅
- 병합 셀(Merged Cell) 자동 처리

---

## 엑셀 파일 생성 예제

```java
ExcelHeaderNode root = new ExcelHeaderNode("고객정보", null, Arrays.asList(
    new ExcelHeaderNode("이름", "name", null, null),
    new ExcelHeaderNode("나이", "age", null, null),
    new ExcelHeaderNode("이메일", "email", null, null)
), null);

List<UserDTO> users = Arrays.asList(
    new UserDTO("홍길동", 30, "hong@example.com"),
    new UserDTO("김철수", 25, "kim@example.com")
);

try (OutputStream out = new FileOutputStream("test.xlsx")) {
    ExcelGenerator.generateExcel(out, root, users, new HashMap<>());
}
```

✅ **엑셀 파일은 생성 시 자동으로 열 너비가 조정됩니다.**

---

## 주의사항
- `ExcelHeaderNode` 트리의 **leaf 노드 순서**가 엑셀 컬럼 순서를 결정합니다.
- 엑셀을 생성할 때 추가로 `sheet.autoSizeColumn()`을 호출할 필요 없습니다.
- 모든 열(컬럼)이 자동으로 최적화된 너비를 가집니다.

---

## 추가 기능 (추후 지원 예정)
- 특정 열만 autoSize 적용하는 옵션
- 고정 열 너비 설정 옵션
- 셀 스타일 다중 적용
- 다국어 지원 (다국어 엑셀 헤더 변환)

```

---

# ✅ 요약
- 최신 `generateExcel()` 구조 반영 완료
- 자동 열 너비 조정 설명 명확하게 추가
- 불필요한 manual autoSize 설명 삭제
- 추가 발전 방향 힌트도 포함
