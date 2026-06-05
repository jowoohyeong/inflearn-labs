# Spring Boot + Elasticsearch + JPA 연동 실습 프로젝트

본 프로젝트는 **Spring Boot 4.0**과 **Java 21** 환경에서 **JPA(MySQL)** 및 **Elasticsearch**를 연동하여 검색 시스템을 구현하는 예제 프로젝트입니다.  
데이터의 영속성 관리를 위한 RDB(MySQL)와 고성능 전문(Full-Text) 검색을 위한 Elasticsearch를 함께 사용하며, 다양한 검색 최적화 기술(동의어 처리, 자동완성, 하이라이팅 등)을 실습할 수 있도록 구성되어 있습니다.

---

## 🛠 기술 스택

* **Backend**: Java 21, Spring Boot 4.0.0
* **Build Tool**: Gradle
* **Database & Search**:
  * **MySQL** (상품 데이터 영속성 관리)
  * **Elasticsearch 7.17.20** (상품 및 사용자 검색 엔진)
  * **Kibana 7.17.20** (Elasticsearch 데이터 시각화 및 개발 도구)
* **Libraries**:
  * Spring Data JPA
  * Spring Data Elasticsearch
  * Lombok

---

## 📂 프로젝트 구조

```text
src/main/
├── java/com/example/elastic/
│   ├── ElasticApplication.java           # 메인 애플리케이션 클래스
│   ├── product/                          # 상품(Product) 모듈
│   │   ├── domain/
│   │   │   ├── Product.java              # MySQL 테이블 매핑 JPA 엔티티
│   │   │   └── ProductDocument.java      # Elasticsearch 인덱스 매핑 도큐먼트
│   │   ├── dto/
│   │   │   └── CreateProductRequestDto.java
│   │   ├── ProductController.java        # REST API 컨트롤러
│   │   ├── ProductService.java           # 상품 비즈니스 로직 (RDB-ES 이중 쓰기 및 검색 구현)
│   │   ├── ProductRepository.java        # JPA Repository (MySQL)
│   │   └── ProductDocumentRepository.java # Elasticsearch Repository (ES)
│   └── user/                             # 사용자(User) 모듈
│       ├── UserController.java           # User ES 도큐먼트 CRUD 컨트롤러
│       ├── UserCreateRequestDto.java
│       ├── UserUpdateRequestDto.java
│       ├── UserDocument.java             # User ES 도큐먼트 엔티티
│       └── UserDocumentRepository.java   # User ES Repository
└── resources/
    ├── application.yml                   # DB 및 Elasticsearch 설정 파일
    └── elasticsearch/
        ├── Dockerfile                    # Nori 형태소 분석기가 포함된 ES 빌드 파일
        ├── docker-compose.yml            # ES + Kibana 컨테이너 설정 파일
        └── product-settings.json         # 한글 형태소 분석기(Nori), HTML 태그 제거, 동의어 사전 설정
```

---

## 💡 핵심 기능 및 설계 패턴

### 1. RDB & Elasticsearch 이중 쓰기 (Dual Write Pattern)
* 상품 생성 시, 관계형 데이터베이스([ProductRepository](src/main/java/com/example/elastic/product/ProductRepository.java))에 데이터를 저장하고, 그 식별자(ID)를 매핑하여 검색 인덱스([ProductDocumentRepository](src/main/java/com/example/elastic/product/ProductDocumentRepository.java))에도 동시에 동기화합니다.
* 상품 삭제 시에도 동일하게 MySQL과 Elasticsearch 양쪽에서 데이터를 삭제하여 일관성을 유지합니다.

### 2. 한글 형태소 분석기 및 검색 최적화
* **Nori 형태소 분석기**: 한글 검색을 명확하게 처리하기 위해 Docker를 통해 Elasticsearch에 `analysis-nori` 플러그인을 적용하였습니다.
* **HTML 태그 제거 (`html_strip`)**: 상품 상세 설명에 웹 에디터 등으로 작성된 HTML 태그가 포함되어도 검색 색인 과정에서 태그를 분리 및 제거하여 텍스트 내용으로만 매칭되도록 설계되었습니다.
* **동의어 사전(Synonyms) 설정**: `apple - 애플`, `samsung - 삼성`, `노트북 - 랩탑 - 컴퓨터` 등 유의어 및 한/영 혼용 단어에 대한 검색 연관성을 높였습니다.

### 3. 실시간 자동완성 (Search-As-You-Type)
* 상품 이름 필드에 `search_as_you_type` 자료형과 Nori 분석기를 조합하여, 검색창에 타이핑하는 도중 실시간으로 연관 상품명을 추천해 주는 제안 API(`/products/suggestions`)를 제공합니다.

### 4. 고급 검색 Query DSL 활용 ([ProductService](src/main/java/com/example/elastic/product/ProductService.java))
* **가중치 부여**: 상품명 검색(`name^3`)에 가장 높은 가중치를, 카테고리(`category^2`)와 설명(`description^1`) 순으로 가중치를 차등 분배하여 정확도를 극대화했습니다.
* **오타 허용 검색 (Fuzziness)**: 유저가 검색어를 입력할 때 오타가 있어도 자동 보정하여 일치율이 높은 상품을 검색합니다 (`fuzziness("AUTO")`).
* **필터 가공**: 카테고리 필터(`TermQuery`) 및 가격 범위 필터(`NumberRangeQuery`)를 조건별로 동적 적용합니다.
* **평점 가산 (Should Clause)**: 평점이 4.0 이상인 상품을 `should` 조건에 넣어 검색 랭킹(Score)에서 가산점을 얻을 수 있도록 설정하였습니다.
* **하이라이팅(Highlighting)**: 검색 결과 내 매칭된 검색어 부분을 `<b>...</b>` 태그로 묶어 프론트엔드에 전달함으로써 가시성을 높였습니다.

---

## 🚀 시작 가이드

### 1. 인프라 환경 기동 (Docker)
로컬 환경에 Docker 및 Docker Compose가 설치되어 있어야 합니다.

```bash
# 설정 폴더로 이동하여 컨테이너 실행 (Elasticsearch, Kibana, MySQL/MariaDB)
cd src/main/resources/elasticsearch
docker-compose up -d
```
* **Elasticsearch 주소**: `http://localhost:9200`
* **Kibana 주소**: `http://localhost:5601` (Kibana Dev Tools 등 사용 가능)
* **MySQL/MariaDB 포트**: `3307` (컨테이너 외부 포트, 비밀번호: `1234`)
  * `docker-compose.yml` 실행 시 `coupang` 데이터베이스가 자동으로 생성됩니다.

### 2. 데이터베이스 설정 (MySQL/MariaDB)
* 데이터베이스가 Docker 컨테이너 외부 포트 `3307`로 실행되므로, [application.yml](src/main/resources/application.yml) 파일에서 datasource URL을 다음과 같이 `3307` 포트로 지정해 주어야 합니다.
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3307/coupang
      username: root
      password: 1234
      driver-class-name: com.mysql.cj.jdbc.Driver
  ```
* 애플리케이션 실행 시 `ddl-auto: update` 설정에 의해 `products` 테이블이 자동으로 생성됩니다.

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

---

## 🔌 API 명세 및 테스트 방법

### 🖥 Web Search Console (대시보드 웹페이지)
상품 등록, 자동완성 검색, 삭제 및 재색인 제어를 마우스 클릭으로 쉽게 조작할 수 있는 대화형 웹 인터페이스를 제공합니다.
* **대시보드 접속 주소**: [http://localhost:8080/](http://localhost:8080/) (또는 [http://localhost:8080/index.html](http://localhost:8080/index.html))

### 📖 Swagger UI (대화형 API 테스트 및 명세)
프로젝트에 **Springdoc OpenAPI (Swagger)**가 연동되어 있어 웹 브라우저에서 직접 시각적으로 테스트할 수 있습니다.
* **Swagger UI 주소**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* **OpenAPI 3.0 스펙 문서 (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

또한, 아래 예시 cURL 명령어를 참고하여 터미널에서도 직접 API를 테스트할 수 있습니다.

### 🛍 상품(Product) API

#### 1. 상품 등록 (RDB 저장 + ES 색인)
* **메서드/경로**: `POST /products`
* **요청 바디**:
```json
{
  "name": "삼성 갤럭시 북4",
  "description": "최신 고성능 노트북 컴퓨터입니다. <b>초특가!</b>",
  "price": 1450000,
  "rating": 4.8,
  "category": "노트북"
}
```
* **cURL 예시**:
```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name": "삼성 갤럭시 북4", "description": "최신 고성능 노트북 컴퓨터입니다. <b>초특가!</b>", "price": 1450000, "rating": 4.8, "category": "노트북"}'
```

#### 2. 상품 페이징 조회 (RDB 기반)
* **메서드/경로**: `GET /products?page=1&size=10`
* **cURL 예시**:
```bash
curl -X GET "http://localhost:8080/products?page=1&size=10"
```

#### 3. 상품명 실시간 자동완성 제안 (ES 기반)
* **메서드/경로**: `GET /products/suggestions?query={검색어일부}`
* **cURL 예시**:
```bash
curl -X GET "http://localhost:8080/products/suggestions?query=삼"
```

#### 4. 상품 조건 검색 및 하이라이팅 (ES 기반)
* **메서드/경로**: `GET /products/search?query={검색어}&category={카테고리}&minPrice={최소값}&maxPrice={최대값}&page={페이지}&size={사이즈}`
* **cURL 예시**:
```bash
curl -X GET "http://localhost:8080/products/search?query=노트북&category=노트북&minPrice=1000000&maxPrice=2000000&page=1&size=5"
```

#### 5. 상품 삭제 (RDB 삭제 + ES 색인 삭제)
* **메서드/경로**: `DELETE /products/{id}`
* **cURL 예시**:
```bash
curl -X DELETE http://localhost:8080/products/1
```

#### 6. 상품 전체 재색인 (RDB -> ES 벌크 색인)
* **메서드/경로**: `POST /products/reindex?reset={true|false}&chunkSize={크기}`
  * `reset=true` 설정 시 기존 ES 인덱스를 완전 삭제 후 맵핑까지 재생성하여 초기화합니다. (기본값: `false`)
  * `chunkSize`는 한 번에 처리할 배치 크기입니다. (기본값: `1000`)
* **cURL 예시**:
```bash
curl -X POST "http://localhost:8080/products/reindex?reset=true&chunkSize=1000"
```

---

### 👤 사용자(User) API

#### 1. 사용자 등록 (ES 단독 저장)
* **메서드/경로**: `POST /users`
* **요청 바디**:
```json
{
  "id": "user123",
  "name": "홍길동",
  "age": 28,
  "isActive": true
}
```
* **cURL 예시**:
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"id": "user123", "name": "홍길동", "age": 28, "isActive": true}'
```

#### 2. 사용자 전체 조회 (ES 페이징)
* **메서드/경로**: `GET /users`
* **cURL 예시**:
```bash
curl -X GET http://localhost:8080/users
```

#### 3. 사용자 단건 조회 (ES)
* **메서드/경로**: `GET /users/{id}`
* **cURL 예시**:
```bash
curl -X GET http://localhost:8080/users/user123
```

#### 4. 사용자 정보 수정 (ES)
* **메서드/경로**: `PUT /users/{id}`
* **요청 바디**:
```json
{
  "name": "이순신",
  "age": 32,
  "isActive": false
}
```
* **cURL 예시**:
```bash
curl -X PUT http://localhost:8080/users/user123 \
  -H "Content-Type: application/json" \
  -d '{"name": "이순신", "age": 32, "isActive": false}'
```

#### 5. 사용자 삭제 (ES)
* **메서드/경로**: `DELETE /users/{id}` 
* **cURL 예시**:
```bash
curl -X DELETE http://localhost:8080/users/user123
```

