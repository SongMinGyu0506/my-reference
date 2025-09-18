# 📑 최종 정리: `CustomPOI_API_DOC.md` (Notion 스타일, 전체 Public 메소드 문서화)

## 1. 개요
- CustomPOI는 다양한 형태의 엑셀 파일을 생성하고 읽을 수 있는 유틸리티입니다.
- 다단계 헤더, 병합 셀, DTO 매핑 기반 엑셀 자동화 지원
- 자동 열 너비 조정 기능 포함 (autoSizeColumn)

---

## 2. ExcelGenerator 클래스

### 2.1. 메소드: generateExcel

#### 2.1.1. 시그니처
```java
public static void generateExcel(OutputStream out, ExcelHeaderNode headerRoot, List<?> dataList, Map<String, ExcelStyle> bodyStyleMap) throws Exception
```

#### 2.1.2. 기능 설명
- 주어진 `headerRoot`를 기반으로 엑셀 헤더를 작성합니다.
- `dataList`를 기반으로 바디(데이터) 영역을 채웁니다.
- `bodyStyleMap`을 통해 각 필드별 스타일을 적용할 수 있습니다.
- 작성 완료 후 **컬럼 너비를 자동으로 조정(autoSizeColumn)** 합니다. ✅

---

#### 2.1.3. 파라미터

| 파라미터명 | 타입 | 설명 |
|:---|:---|:---|
| out | OutputStream | 엑셀 파일을 출력할 스트림 |
| headerRoot | ExcelHeaderNode | 엑셀 헤더를 정의하는 트리 최상위 노드 |
| dataList | List<?> | 엑셀에 작성할 DTO 객체 리스트 |
| bodyStyleMap | Map<String, ExcelStyle> | 필드별 바디 셀 스타일 설정 맵 |

---

#### 2.1.4. 특징
- 다단계 헤더 작성 가능
- 병합 셀(Merged Cell) 지원
- DTO 필드명과 매칭하여 데이터 자동 작성
- **엑셀 저장 직전에 열 너비 자동 최적화** 적용

---

#### 2.1.5. 주의사항
- `ExcelHeaderNode` 트리의 **leaf 노드 순서**가 컬럼 순서를 결정합니다.
- DTO 클래스의 필드명과 `ExcelHeaderNode`의 `fieldName`이 정확히 매칭되어야 합니다.
- 추가적인 열 너비 조정 코드가 필요 없습니다. (내부 autoSize 적용)

---

### 2.2. 메소드: parseExcelToDto

#### 2.2.1. 시그니처
```java
public static <T> List<T> parseExcelToDto(InputStream in, Class<T> dtoClass, int headerEndRow, ExcelHeaderNode headerRoot) throws Exception
```

#### 2.2.2. 기능 설명
- 엑셀 파일을 읽어 `dtoClass` 타입의 리스트로 변환합니다.
- `headerEndRow` 이후부터 데이터 영역으로 인식하여 데이터를 읽습니다.
- `headerRoot`를 기반으로 **컬럼 순서**와 **DTO 필드 매핑**을 수행합니다.

---

#### 2.2.3. 파라미터

| 파라미터명 | 타입 | 설명 |
|:---|:---|:---|
| in | InputStream | 읽어올 엑셀 파일 입력 스트림 |
| dtoClass | Class<T> | 변환할 DTO 클래스 타입 |
| headerEndRow | int | 헤더가 끝나는 행(row) 인덱스 (0부터 시작) |
| headerRoot | ExcelHeaderNode | 헤더 구조를 정의하는 트리 최상위 노드 |

---

#### 2.2.4. 특징
- **Row 단위**로 엑셀 데이터를 읽어 DTO로 변환
- 병합 셀(Merged Cell) 영역에서도 정상적으로 값을 읽어옴
- 데이터 셀 비어있을 경우, 병합된 상단 셀에서 값 가져옴
- DTO 필드 타입(Integer, Double, String 등)에 맞춰 자동 변환 지원
- 변환 실패 시 `null` 처리 (예외 발생 없이 무시)

---

#### 2.2.5. 주의사항
- DTO 필드명과 `ExcelHeaderNode`의 fieldName이 정확히 매칭되어야 함
- 데이터 Row의 컬럼 순서가 헤더 leaf 노드 순서와 맞아야 함
- 변환할 수 없는 값이 존재할 경우 해당 필드는 `null`로 채워질 수 있음

---

## 3. 추가 예정 기능

- 특정 컬럼만 선택적으로 autoSize 적용
- 고정 열 너비 설정
- 헤더 다국어 지원 (i18n)
- 다양한 셀 스타일 템플릿 지원

---

# ✅ 요약
- `generateExcel`과 `parseExcelToDto` 두 개 모두 문서화 완료
- 시그니처, 설명, 파라미터, 특징, 주의사항 모두 빠짐없이 정리
- 형식은 Notion 스타일 (계층적 마크다운)