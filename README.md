# atmd API
---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.3 (Spring Framework 6.2.x, Jakarta EE) |
| ORM | Spring Data JPA + Hibernate, QueryDSL 5.1.0 |
| DB | PostgreSQL 16 (Flyway 마이그레이션) |
| Cache | Redis 7 (Refresh Token 저장, OAuth state 검증, 패스워드 없이 운영) |
| Auth | Spring Security, JWT (JJWT 0.12.6), OAuth2 (Kakao / Google) |
| Storage | AWS S3 SDK v2 + CloudFront |
| AI | OpenAI API (OpenFeign 클라이언트) |
| CI/CD | GitHub Actions → Docker Hub → EC2 (SSM) |
| Monitoring | Actuator + Micrometer (Prometheus) |
| Docs | Springdoc OpenAPI (Swagger UI, prod 비활성화) |
| Utilities | Lombok, P6Spy (dev only), Spring Cloud OpenFeign |

---

## 패키지 구조

도메인 중심 설계(DDD). 각 도메인이 자신의 레이어를 모두 포함하는 flat 구조.

```
src/main/java/com/projbase/api/
├── ApiApplication.java                     # @SpringBootApplication, @EnableJpaAuditing
│                                           # @EnableFeignClients(defaultConfiguration=FeignConfig)
│
├── domain/
│   ├── auth/                               # 인증 (로그인/회원가입/토큰)
│   │   ├── controller/AuthController.java
│   │   ├── dto/request/                    # LoginRequestDTO, SignupRequestDTO
│   │   ├── dto/response/AuthTokenResponseDTO.java
│   │   ├── exception/AuthErrorCode.java
│   │   └── service/AuthService.java
│   │
│   ├── user/                               # 사용자 도메인
│   │   ├── controller/UserController.java
│   │   ├── dto/response/UserProfileResponseDTO.java
│   │   ├── entity/User.java                # ofLocal(), ofOAuth() 팩토리 메서드
│   │   ├── entity/enums/                   # Provider(LOCAL|KAKAO|GOOGLE), Role(USER|ADMIN)
│   │   ├── exception/UserErrorCode.java
│   │   ├── repository/UserRepository.java  # IsDeletedFalse 조건 포함 쿼리만 사용
│   │   └── service/UserService.java        # findById → findByIdAndIsDeletedFalse 적용
│   │
│   ├── image/                              # S3 이미지 업로드
│   │   ├── controller/ImageController.java
│   │   ├── dto/response/ImageUploadResponseDTO.java
│   │   ├── enums/UploadDirectory.java      # 허용 디렉토리 allowlist (PROFILES, ATTACHMENTS)
│   │   ├── exception/ImageErrorCode.java
│   │   └── service/ImageService.java
│   │
│   └── example/                            # 도메인 템플릿 (참고용)
│       ├── controller/ExampleController.java
│       ├── dto/request/ExampleRequestDTO.java
│       ├── dto/response/ExampleResponseDTO.java
│       ├── entity/Example.java
│       ├── exception/ExampleErrorCode.java
│       ├── repository/ExampleRepository.java
│       └── service/ExampleService.java
│
└── global/
    ├── aop/LoggingAspect.java              # @RestController 전체: method/uri/userId/handler/elapsed 로깅
    ├── auth/
    │   ├── cookie/CookieProvider.java      # RT 쿠키 생성/만료 (HttpOnly, Secure, SameSite=None)
    │   ├── jwt/
    │   │   ├── JwtProvider.java            # AT/RT 생성·검증·파싱
    │   │   │                               # sub에 userId 저장 (중복 claim 없음)
    │   │   │                               # isTokenExpired()로 만료/위변조 구분
    │   │   ├── JwtService.java             # Redis RT:{userId} 저장/검증/삭제
    │   │   ├── JwtProperties.java          # @ConfigurationProperties(prefix="jwt")
    │   │   └── filter/JwtAuthenticationFilter.java  # access 타입 토큰만 인증에 허용
    │   ├── oauth/
    │   │   ├── service/OAuthService.java   # state CSRF 방어, RestClient 기반 토큰 교환
    │   │   │                               # 네트워크 에러 → OAUTH_PROCESS_FAILED 래핑
    │   │   ├── dto/                        # OAuth 응답 DTO (Kakao, Google)
    │   │   └── properties/                 # KakaoOAuthProperties, GoogleOAuthProperties
    │   └── util/SecurityUtil.java          # getCurrentUserId() from SecurityContext
    ├── common/
    │   ├── entity/BaseTimeEntity.java      # created_at, updated_at (@JpaAuditing)
    │   ├── entity/BaseEntity.java          # BaseTimeEntity + is_deleted, deleted_at (소프트 삭제)
    │   ├── exception/
    │   │   ├── BaseErrorCode.java          # interface: getStatus(), getCode(), getMessage()
    │   │   ├── CommonErrorCode.java        # 외부 API 공통 에러 (EXTERNAL_API_ERROR, UNAVAILABLE)
    │   │   ├── GeneralException.java       # RuntimeException wrapping BaseErrorCode
    │   │   └── GlobalExceptionHandler.java # @RestControllerAdvice
    │   └── response/ApiResponse.java       # 공통 응답 래퍼 { success, code, message, data }
    ├── config/
    │   ├── FeignConfig.java                # 전역 Feign 설정 (타임아웃, 로깅, ErrorDecoder)
    │   │                                   # @Configuration 없음 — defaultConfiguration 전용
    │   ├── SecurityConfig.java             # Spring Security 설정, CORS, JWT 필터
    │   ├── RedisConfig.java                # RedisTemplate<String, String>
    │   ├── QuerydslConfig.java             # JPAQueryFactory Bean
    │   ├── SwaggerConfig.java              # OpenAPI Bearer JWT 설정 (prod 비활성화)
    │   ├── WebConfig.java                  # WebMvcConfigurer
    │   └── p6spy/P6SpySqlLogFormatter.java # SQL 로그 포맷 (dev only)
    ├── external/
    │   └── ai/                             # OpenAI API 클라이언트 (OpenFeign)
    │       ├── client/AiClient.java        # @FeignClient → /v1/chat/completions
    │       ├── config/AiConfig.java        # API Key 인터셉터 (FeignConfig와 병행 적용)
    │       ├── dto/request/               # ChatRequestDTO, ChatMessageDTO
    │       ├── dto/response/              # ChatResponseDTO, ChatChoiceDTO
    │       ├── properties/AiProperties.java
    │       └── service/AiService.java      # chat(system, user) / chat(user) 래퍼
    ├── filter/
    │   ├── MDCLoggingFilter.java           # 요청별 8자리 traceId (MDC, 내부 전용)
    │   └── OriginValidationFilter.java     # /auth/refresh, /auth/logout CORS Origin 검증
    └── s3/
        ├── config/S3Config.java            # S3Client Bean
        ├── config/AwsProperties.java       # @ConfigurationProperties(prefix="aws")
        └── service/S3Service.java          # MIME/확장자/매직바이트 검증
                                            # contentType은 확장자 기반 서버 측 고정 (클라이언트 값 미사용)
```

### 도메인 추가 규칙

```
domain/{feature}/
  ├── controller/   → @RestController, 라우팅만 담당
  ├── dto/          → request/, response/ 분리. 네이밍: {Feature}RequestDTO, {Feature}ResponseDTO
  ├── entity/       → @Builder + @NoArgsConstructor(PROTECTED), 비즈니스 로직 포함
  ├── exception/    → {Feature}ErrorCode implements BaseErrorCode
  ├── repository/   → JpaRepository + QueryDSL (필요 시). 소프트 딜리트 시 IsDeletedFalse 조건 필수
  └── service/      → 트랜잭션 관리, 비즈니스 처리
```

---

## 로컬 개발 환경 세팅

### 1. 환경 변수 설정

```bash
cp .env.example .env
# .env 파일을 열어 값 채우기
```

### 2. 인프라 컨테이너 실행 (PostgreSQL + Redis)

```bash
docker compose -f docker-compose-dev.yaml up -d
```

### 3. 앱 실행

```bash
./gradlew bootRun
```

앱은 `.env`의 `DB_HOST=localhost`, `REDIS_HOST=localhost`로 로컬 컨테이너에 연결됨.

> `docker compose -f docker-compose-dev.yaml down` 으로 인프라 종료.

### 4. Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

> Swagger는 `prod` 프로파일에서 자동 비활성화됨.

---

## 프로파일

| 파일 | 용도 | ddl-auto | Flyway | P6Spy | Swagger |
|------|------|----------|--------|-------|---------|
| `application.yaml` | 공통 설정 | `validate` | ON | ON | ON |
| `application-dev.yaml` | 로컬 개발 | `update` | **OFF** | ON | ON |
| `application-prod.yaml` | 프로덕션 | `validate` | ON | **OFF** | **OFF** |

> dev 프로파일: Flyway 비활성화, Hibernate `ddl-auto: update`로 스키마 자동 관리. prod 프로파일: Flyway가 배포 시 자동 실행.

---

## 환경 변수

| 변수 | 설명 | 예시 |
|------|------|------|
| `DB_HOST` | PostgreSQL 호스트 | `localhost` |
| `DB_PORT` | PostgreSQL 포트 | `5432` |
| `DB_NAME` | 데이터베이스 이름 | `projbase` |
| `DB_USER` | DB 사용자 | `projbase` |
| `DB_PASSWORD` | DB 비밀번호 | |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `REDIS_PORT` | Redis 포트 | `6379` |
| `JWT_SECRET` | JWT 서명 키 (32자 이상 권장) | |
| `JWT_ACCESS_EXPIRATION` | AT 만료 시간 (ms) | `3600000` (1h) |
| `JWT_REFRESH_EXPIRATION` | RT 만료 시간 (ms) | `1209600000` (14d) |
| `KAKAO_CLIENT_ID` | 카카오 REST API 키 | |
| `KAKAO_CLIENT_SECRET` | 카카오 Client Secret | |
| `KAKAO_REDIRECT_URI` | 카카오 리다이렉트 URI | |
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID | |
| `GOOGLE_CLIENT_SECRET` | Google OAuth 클라이언트 시크릿 | |
| `GOOGLE_REDIRECT_URI` | Google 리다이렉트 URI | |
| `AWS_REGION` | AWS 리전 | `ap-northeast-2` |
| `AWS_S3_BUCKET` | S3 버킷 이름 | |
| `AWS_CLOUDFRONT_DOMAIN` | CloudFront 도메인 | `d1234abcd.cloudfront.net` |
| `OPENAI_API_KEY` | OpenAI API 키 | |
| `MAIL_HOST` | SMTP 호스트 | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP 포트 | `587` |
| `MAIL_USERNAME` | 발신 이메일 계정 | |
| `MAIL_PASSWORD` | SMTP 앱 비밀번호 | |
| `DOCKERHUB_USERNAME` | Docker Hub 사용자명 (prod 배포용) | |

> Redis는 패스워드 없이 운영. `docker-compose-dev.yaml`, `docker-compose-prod.yaml` 모두 `--requirepass` 미설정.
> Spring Mail은 Gmail 기준 STARTTLS(587 포트) 기본 설정. 미사용 시 환경 변수를 빈 값으로 두면 됨 (앱 기동은 가능하나 메일 발송 시 오류 발생).

---

## 인증 구조

### 토큰 전략

```
AT (Access Token)   → Response Body JSON  → 프론트 메모리 저장  (type: "access")
RT (Refresh Token)  → HTTP-only Cookie    → Redis RT:{userId}  (type: "refresh")
```

- JWT payload는 `sub`에 userId 저장. 별도 `userId` claim 없음.
- AT와 RT에 `type` 클레임이 포함되어 RT를 Authorization 헤더에 사용하면 필터에서 명시적으로 거부됨.
- Refresh Token Rotation 적용: 재발급 시마다 새 RT 생성 후 Redis 덮어쓰기 → 구 RT 즉시 무효화.

### 흐름

```
[로그인 / 회원가입 / OAuth]
Client → POST /api/v1/auth/{endpoint}
       → issueTokens()
           ├── AT 생성 (HMAC-SHA256, sub=userId, role, type:"access")
           ├── RT 생성 → Redis RT:{userId} 저장 (TTL 14일)
           └── Set-Cookie: refresh_token (HttpOnly, Secure, SameSite=None, Path=/api/v1/auth)
       → Response Body: { "accessToken": "...", "tokenType": "Bearer" }

[인증 요청]
Client → Authorization: Bearer {accessToken}
       → JwtAuthenticationFilter
           ├── Bearer 파싱 → validateToken()
           ├── isAccessToken() 확인 (type != "access" 이면 거부)
           ├── sub에서 userId 추출, role 추출
           └── SecurityContext 에 세팅

[토큰 재발급]
Client → POST /api/v1/auth/refresh (쿠키 자동 전송)
       → validateToken(RT)
           ├── 실패: isTokenExpired()로 만료/위변조 구분 → 각각 EXPIRED_TOKEN / INVALID_TOKEN
       → isRefreshToken() 확인
       → Redis RT 대조 (불일치 시 INVALID_TOKEN — 이미 rotate된 토큰 재사용 감지)
       → 새 AT/RT 발급 (Rotation)

[로그아웃]
Client → POST /api/v1/auth/logout (AT 필요)
       → Redis RT:{userId} 삭제 + 쿠키 maxAge=0
```

> **SameSite=None 주의:** `Secure=true`와 함께 동작하므로 HTTPS 필수. Chrome/Firefox는 localhost HTTP에서도 동작하지만, Safari 로컬 개발 시 HTTPS 환경 필요.

### OAuth 흐름 (CSRF 방어 포함)

```
1. Client → GET /api/v1/auth/oauth2/{provider}/authorize
         → 서버: state 생성 → Redis OAUTH_STATE:{state} 저장 (TTL 5분)
         → { authorizeUrl: "...?state={state}" }

2. Client → 브라우저에서 authorizeUrl 이동
         → OAuth 제공자 → GET /api/v1/auth/oauth2/{provider}/callback?code=xxx&state={state}
                          (백엔드 redirect_uri 직접 호출 — www.api.{domain})
         → 서버: Redis에서 state 검증 + 일회성 삭제 (getAndDelete)
         → 코드 교환 → 사용자 정보 조회 → 토큰 발급
         → 네트워크 에러/타임아웃 → OAUTH_PROCESS_FAILED(500) 로 래핑
```

지원 provider: `kakao`, `google`

OAuth 외부 요청(토큰 교환, 사용자 정보) 타임아웃: connect 3초 / read 5초

**Google:** `email_verified: false`인 계정은 `OAUTH_UNVERIFIED_EMAIL(400)` 에러 반환.

**Kakao:** 이메일 동의를 받지 못한 경우 `kakao_{id}@kakao.user` 형식의 가상 이메일로 계정 생성.

---

## API 엔드포인트

### 인증 (`/api/v1/auth`) — 인증 불필요

| Method | Path | 설명 |
|--------|------|------|
| POST | `/signup` | 네이티브 회원가입 → AT 반환 |
| POST | `/login` | 네이티브 로그인 → AT 반환 |
| POST | `/logout` | 로그아웃 (AT 필요) |
| POST | `/refresh` | RT 쿠키로 AT/RT 재발급 |
| GET | `/oauth2/{provider}/authorize` | OAuth 인가 URL + state 반환 |
| GET | `/oauth2/{provider}/callback` | `?code=xxx&state=xxx` 쿼리 파라미터 → 로그인 처리 |

### 사용자 (`/api/v1/users`) — 인증 필요

| Method | Path | 설명 |
|--------|------|------|
| GET | `/me` | 내 프로필 조회 |

### 이미지 (`/api/v1/images`) — 인증 필요

| Method | Path | 설명 |
|--------|------|------|
| POST | `/{directory}` | 이미지 업로드 → CloudFront URL 반환 |

`directory` 허용값: `profiles`, `attachments` (그 외는 400 응답)

### 공통 응답 형식

```json
{ "success": true,  "code": "200", "message": "요청이 성공했습니다.", "data": { ... } }
{ "success": false, "code": "AUTH_401_INVALID_TOKEN", "message": "...", "data": null }
```

### GlobalExceptionHandler 처리 케이스

| 예외 | HTTP | 에러 코드 | 설명 |
|------|------|-----------|------|
| `GeneralException` | 도메인별 | 도메인별 코드 | 비즈니스 로직 예외 |
| `MethodArgumentNotValidException` | 400 | `COMMON_400_VALIDATION` | `@Valid` Bean Validation 실패 |
| `MethodArgumentTypeMismatchException` | 400 | `COMMON_400_TYPE_MISMATCH` | 경로/쿼리 파라미터 타입 불일치 |
| `HttpMessageNotReadableException` | 400 | `COMMON_400_BODY_NOT_READABLE` | JSON 파싱 실패 |
| `ConstraintViolationException` | 400 | `COMMON_400_CONSTRAINT` | `@Validated` 메서드 레벨 검증 실패 |
| `MissingRequestCookieException` | 401 | `AUTH_401_MISSING_COOKIE` | `refresh_token` 쿠키 누락 |
| `DataIntegrityViolationException` | 409 | `COMMON_409_DUPLICATE` | DB 유니크 제약 위반 |
| `MaxUploadSizeExceededException` | 413 | `COMMON_413_FILE_TOO_LARGE` | 파일 크기 초과 (10MB 제한) |
| `Exception` (미처리) | 500 | `COMMON_500` | 서버 내부 오류 |

---

## 이미지 업로드 (S3 + CloudFront)

### 역할 분리

| 주체 | 담당 |
|------|------|
| 프론트엔드 | 사이트 로고, 온보딩 이미지 등 정적 에셋 직접 관리 |
| 백엔드 | 사용자 업로드 이미지 (프로필, 첨부파일 등) |

### 업로드 흐름

```
Client → POST /api/v1/images/{directory} (multipart/form-data, key: "file")
       → 디렉토리 allowlist 검증 (UploadDirectory enum)
       → MIME 타입 + 확장자 검증 (jpeg, png, gif, webp)
       → 매직바이트(파일 시그니처) 검증
       → S3 업로드 키: {directory}/{UUID}.{ext}  ← 원본 파일명 제거
       → contentType: 확장자 기반 서버 측 결정 (클라이언트 Content-Type 미사용 — XSS 방지)
       → CloudFront URL 반환
```

허용 MIME 타입: `image/jpeg`, `image/png`, `image/gif`, `image/webp`
파일 크기 제한: **10MB** (`max-file-size: 10MB`, `max-request-size: 10MB`). 초과 시 `COMMON_413_FILE_TOO_LARGE` 반환.
S3 Presigned URL 미사용 — 모든 이미지 접근은 CloudFront 도메인을 통함.

---

## AI 연동 (OpenAI)

### 구조

```
global/external/ai/
  ├── client/AiClient.java        # @FeignClient(url="${ai.openai.base-url}", configuration=AiConfig)
  ├── config/AiConfig.java        # Authorization: Bearer {API_KEY} 인터셉터
  ├── dto/request/                # ChatRequestDTO, ChatMessageDTO
  ├── dto/response/               # ChatResponseDTO, ChatChoiceDTO
  ├── properties/AiProperties.java # model, maxTokens 등 설정값
  └── service/AiService.java      # 도메인 서비스에서 주입하여 사용
```

### Feign 전역 설정 (FeignConfig)

모든 `@FeignClient`에 자동 적용되는 공통 설정. `@Configuration` 없이 `defaultConfiguration`으로만 사용.

| 항목 | 값 | 비고 |
|------|-----|------|
| 로깅 레벨 | `Logger.Level.BASIC` | 요청 URL + 응답 상태/시간 |
| Connect Timeout | 3초 | |
| Read Timeout | 60초 | AI API 응답 시간 고려 |
| ErrorDecoder | 5xx → `EXTERNAL_API_UNAVAILABLE(503)` | 에러 발생 시 메서드명+상태코드 로깅 |
|              | 4xx → `EXTERNAL_API_ERROR(502)` | |

외부 API 에러는 `CommonErrorCode`를 통해 `GeneralException`으로 래핑되어 `GlobalExceptionHandler`가 처리.

### 사용법

```java
@Service
@RequiredArgsConstructor
public class SomeService {
    private final AiService aiService;

    public String analyze(String input) {
        return aiService.chat("당신은 분석 전문가입니다.", input);
        // 또는 시스템 프롬프트 생략
        // return aiService.chat(input);
    }
}
```

새 외부 API 연동 시 `global/external/{api-name}/` 패키지 생성. `AiConfig` 패턴 참고.

### 설정

```yaml
ai:
  openai:
    api-key: ${OPENAI_API_KEY:}
    base-url: https://api.openai.com
    model: gpt-4o-mini
    max-tokens: 2048
```

---

## 데이터베이스

### 공통 컬럼 (BaseEntity)

```
BaseTimeEntity   → created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ
BaseEntity       → BaseTimeEntity + is_deleted BOOLEAN DEFAULT false, deleted_at TIMESTAMPTZ
```

소프트 삭제: `entity.softDelete()` 호출 → `is_deleted = true`, `deleted_at = now()`

**새 도메인 Repository 규칙:** 조회 쿼리는 반드시 `IsDeletedFalse` 조건 포함. `findById` 직접 사용 금지.

```java
// 올바른 패턴
Optional<User> findByIdAndIsDeletedFalse(Long id);

// 금지
Optional<User> findById(Long id);  // 탈퇴/삭제 데이터 노출 위험
```

### 마이그레이션 (Flyway)

- 파일 위치: `src/main/resources/db/migration/`
- 네이밍: `V{버전}__{설명}.sql` (예: `V2__add_profile_image.sql`)
- prod 기동 시 Flyway가 자동으로 미적용 마이그레이션을 순서대로 실행

### DB 컨벤션

| 항목 | 규칙 |
|------|------|
| 필드 네이밍 | snake_case |
| 날짜 타입 | TIMESTAMPTZ |
| 소프트 삭제 | is_deleted BOOLEAN + deleted_at TIMESTAMPTZ |
| 타임존 | Asia/Seoul (JVM, Hibernate, PostgreSQL 모두 통일) |

---

## 로깅

### MDC 트레이스 ID

모든 요청에 8자리 UUID 기반 `traceId` 자동 부여. logback 패턴 `%X{traceId}` 로 출력. 내부 전용 (응답 헤더 미노출).

### AOP 요청 로깅

`@RestController` 메서드 전체에 자동 적용. userId는 SecurityContext에서 추출 (비인증 요청은 `anonymous`).

```
[GET]  /api/v1/users/me  | user=42    | 15ms  | handler=UserController.getMyProfile()
[POST] /api/v1/auth/login | user=anonymous | 42ms | handler=AuthController.login(..)
[POST] /api/v1/images/profiles | user=7 | 230ms | handler=ImageController.upload(..)
```

### Feign 외부 API 로깅

`Logger.Level.BASIC`으로 외부 API 요청/응답 상태 로깅.
`application.yaml`의 `logging.level.com.projbase.api.global.external: DEBUG` 로 활성화.

에러 발생 시: `[External API Error] AiClient#chat(ChatRequestDTO) → status=429`

### 프로파일별 로그

| 환경 | 출력 | 레벨 |
|------|------|------|
| dev | 컬러 콘솔 | DEBUG |
| prod | 롤링 파일 (100MB / 30일 보관) + 콘솔 | INFO |

---

## Docker Compose

| 파일 | 용도 |
|------|------|
| `docker-compose-dev.yaml` | **로컬 개발용 인프라** (PostgreSQL + Redis만). 앱은 `./gradlew bootRun` |
| `docker-compose-prod.yaml` | **프로덕션 전체 스택** (app + PostgreSQL + Redis). CD 워크플로우가 EC2에 전송 |

```bash
# 로컬 개발
docker compose -f docker-compose-dev.yaml up -d
./gradlew bootRun

# 프로덕션 (EC2에서)
docker compose -f docker-compose-prod.yaml --env-file .env.prod up -d
```

### 프로덕션 보안 구성

- **앱 포트**: `127.0.0.1:8080:8080` — 리버스 프록시(Nginx 등)만 접근 가능
- **PostgreSQL**: 포트 미노출 — 내부 Docker 네트워크로만 접근
- **Redis**: 포트 미노출, 패스워드 없이 내부 Docker 네트워크로만 접근
- **Actuator (8081)**: 포트 미노출 — Prometheus 및 health check는 Docker 내부 네트워크 전용

### Actuator / Prometheus 포트 구성

| 포트 | 용도 | 외부 노출 |
|------|------|-----------|
| `8080` | 앱 API 트래픽 | Nginx를 통해 노출 |
| `8081` | Actuator (health, prometheus) | Docker 내부 전용 |

### JVM 옵션 (Dockerfile)

| 옵션 | 설명 |
|------|------|
| `-Duser.timezone=Asia/Seoul` | JVM 타임존 고정 |
| `-XX:+UseContainerSupport` | 컨테이너 메모리 제한 인식 |
| `-XX:MaxRAMPercentage=75.0` | 컨테이너 메모리의 75%를 힙 상한으로 설정 |
| `-XX:+ExitOnOutOfMemoryError` | OOM 발생 시 프로세스 종료 → `restart: unless-stopped`로 자동 재기동 |

### Graceful Shutdown

`server.shutdown: graceful` + `timeout-per-shutdown-phase: 30초` 설정됨.

---

## CI/CD

### 브랜치 전략

`develop`이 기본(default) 브랜치. `main`은 프로덕션 배포 전용.

```
이슈 생성  → GitHub Actions → 브랜치 자동 생성 (feat/#12-add-login)
작업 브랜치 → PR → develop   : CI (테스트)
develop    → main            : CD (빌드 → Docker Hub → EC2 배포)
```

### 이슈 브랜치 자동 생성 (`.github/workflows/create-branch-from-issue.yml`)

이슈 오픈 시 자동으로 브랜치 생성.

- 이슈 제목 형식: `[PREFIX] 작업 내용` (예: `[feat] 로그인 기능 추가`)
- 브랜치 이름: `{prefix}/#{이슈번호}-{영문슬러그}` (예: `feat/#12-add-login-feature`)
- 슬러그 번역: Groq API(`groq/compound-mini`)로 한글 → 영어 자동 변환
- 기준 브랜치: `develop`

필요 Secret: `GROQ_API_KEY`

### CI (`.github/workflows/ci.yaml`)

**트리거:** PR → `develop`
`./gradlew clean test` (PostgreSQL 16 + Redis 7 서비스 컨테이너 포함)

### CD (`.github/workflows/cd.yaml`)

**트리거:** push → `main`

1. `./gradlew clean bootJar -x test`
2. Docker Hub 이미지 빌드 & push (`latest` + `{sha}` 태그)
3. AWS OIDC로 임시 자격증명 획득
4. `docker-compose-prod.yaml` → SSM으로 EC2 전송
5. EC2에서 `docker-compose pull/up -d`

---

## GitHub Secrets 등록

GitHub 레포 → Settings → Secrets and variables → Actions → Repository secrets

### CI

| Secret | 설명 |
|--------|------|
| `JWT_SECRET` | JWT 서명 키 |
| `KAKAO_CLIENT_ID` | 카카오 REST API 키 |
| `KAKAO_CLIENT_SECRET` | 카카오 Client Secret |
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth 클라이언트 시크릿 |

### CD

| Secret | 설명 |
|--------|------|
| `DOCKERHUB_USERNAME` | Docker Hub 사용자명 |
| `DOCKERHUB_TOKEN` | Docker Hub Access Token |
| `AWS_DEPLOY_ROLE_ARN` | GitHub OIDC로 Assume할 IAM Role ARN |
| `EC2_INSTANCE_ID` | 배포 대상 EC2 인스턴스 ID |

### 이슈 브랜치 자동화

| Secret | 설명 |
|--------|------|
| `GROQ_API_KEY` | Groq API 키 (이슈 제목 → 브랜치 슬러그 번역) |

### AWS OIDC 사전 설정

1. IAM Identity Provider: `https://token.actions.githubusercontent.com` / Audience: `sts.amazonaws.com`
2. IAM Role Trust policy:
   ```json
   "StringLike": {
     "token.actions.githubusercontent.com:sub": "repo:{org}/{repo}:ref:refs/heads/main"
   }
   ```
   권한: `ssm:SendCommand`, `ssm:GetCommandInvocation`
3. Role ARN → `AWS_DEPLOY_ROLE_ARN` 등록

---

## EC2 배포 환경 사전 준비

```
/home/ubuntu/projbase/
└── .env.prod     # 프로덕션 환경 변수
```

### 배포 전 체크리스트

| 항목 | 위치 | 내용 |
|------|------|------|
| CORS 도메인 | `SecurityConfig.java` | `allowedOrigins`에 실제 프론트 도메인 추가 |
| `.env.prod` | EC2 `/home/ubuntu/projbase/` | 모든 필수 환경 변수 채우기 |
| GitHub Secrets | 레포 Settings → Secrets | 위 Secrets 표 참고 |

---

## 코드 컨벤션

### 객체 생성 — 빌더 패턴

```java
// Entity: 팩토리 메서드
public static User ofLocal(String email, String encodedPassword, String nickname) {
    return User.builder().email(email).provider(Provider.LOCAL).role(Role.USER).build();
}

// Response DTO: 정적 팩토리
public static AuthTokenResponseDTO of(String accessToken) {
    return AuthTokenResponseDTO.builder().accessToken(accessToken).tokenType("Bearer").build();
}
```

Request DTO (Jackson 역직렬화 대상)는 `@Getter + @NoArgsConstructor`.

### 예외 처리

```java
// 1. 도메인별 ErrorCode 정의
public enum UserErrorCode implements BaseErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다.");
}

// 2. throw
throw new GeneralException(UserErrorCode.USER_NOT_FOUND);
// GlobalExceptionHandler 가 자동으로 ApiResponse 형식으로 처리
```

외부 인프라(Feign, S3 등) 에러는 `CommonErrorCode` 사용.

---

---

## AI Assistant Prompt

> 이 섹션은 이 프로젝트를 처음 접하는 개발자의 AI 어시스턴트가 컨텍스트를 빠르게 파악하고 **Terraform 인프라 세팅까지 완료**할 수 있도록 작성된 프롬프트입니다. Claude Code, GitHub Copilot, Cursor 등에 붙여 사용하세요.

```
================================================================================
PROJECT CONTEXT — ProjBase API (Spring Boot 3.5.3 Base Template)
================================================================================

이 프로젝트는 Spring Boot 3.5.3 기반 API 베이스 템플릿입니다.
새 서비스를 시작할 때 인증, 이미지 업로드, AI 연동, CI/CD, AWS 인프라가
미리 세팅된 상태에서 바로 도메인 로직만 추가할 수 있도록 설계되었습니다.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. 기술 스택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
- Runtime: Java 17, Spring Boot 3.5.3
- Security: Spring Security 6 (JWT stateless)
- Persistence: Spring Data JPA + PostgreSQL 16 + Flyway 마이그레이션
- Cache: Redis 7 (비밀번호 없음, 내부 네트워크 전용)
- ORM 확장: QueryDSL 5.1.0
- External API: Spring Cloud OpenFeign (전역 FeignConfig 적용)
- AI: OpenAI gpt-4o-mini (AiService.chat() via AiClient Feign)
- File Upload: AWS S3 SDK v2 + CloudFront (OAC)
- Monitoring: Prometheus + Actuator, P6Spy (dev only)
- Build: Gradle 8, Docker / Docker Compose

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
2. 패키지 구조
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
com.projbase.api
├── domain/
│   ├── auth/          controller, service, dto, exception (JWT 발급/재발급/로그아웃, OAuth)
│   ├── user/          controller, service, entity(User), repository, dto, exception
│   ├── image/         controller, service, exception (S3 업로드)
│   └── example/       샘플 도메인 (삭제 후 본인 도메인으로 교체)
└── global/
    ├── auth/          jwt/, oauth/, cookie/, filter/, security/
    ├── config/        SecurityConfig, FeignConfig, QueryDslConfig, ...
    ├── aop/           LoggingAspect (MDC traceId + AOP 요청로그)
    ├── common/        response/ApiResponse, exception/GeneralException, BaseErrorCode
    ├── external/      ai/ (AiClient, AiConfig, AiService) — 외부 API 패키지
    └── s3/            config/, service/S3Service

새 도메인 추가 시: domain/{feature}/ 하위에 위 구조대로 생성.
새 외부 API 추가 시: global/external/{api-name}/ 패키지 생성, AiClient/AiConfig 패턴 참고.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
3. 인증 구조 (JWT + OAuth)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Access Token:
  - 응답 바디 JSON { accessToken: "..." } 으로 전달
  - 프론트엔드는 메모리(변수)에만 저장 — localStorage 금지
  - JWT payload: sub=userId(Long), role claim, type="access"
  - userId claim은 없음 (sub만 사용)

Refresh Token:
  - HttpOnly 쿠키 (SameSite=None, Secure=true, Path=/api/v1/auth)
  - Redis 키: RT:{userId} 에 저장, Rotation마다 덮어쓰기로 구 RT 자동 무효화
  - type="refresh" — AT로 사용 시 명시적 차단

토큰 검증 실패 구분:
  - isTokenExpired() → true: EXPIRED_TOKEN (401)
  - validateToken() → false + 만료 아님: INVALID_TOKEN (401)
  - Redis whitelist 불일치 (reuse): INVALID_TOKEN (401)

OAuth (Kakao, Google):
  - 콜백 엔드포인트: GET /api/v1/auth/oauth2/{provider}/callback?code=xxx&state=xxx (쿼리 파라미터)
  - CSRF 방어: state UUID → Redis TTL 5분 → 콜백 getAndDelete로 일회성 검증
  - 외부 RestClient 호출: RestClientException catch → OAUTH_PROCESS_FAILED(500)
  - Google: emailVerified == false → OAUTH_UNVERIFIED_EMAIL(400)
  - Kakao: 이메일 미동의 → kakao_{id}@kakao.user 가상 이메일로 계정 생성

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
4. 핵심 컨벤션
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
소프트 딜리트:
  - BaseEntity: is_deleted BOOLEAN DEFAULT FALSE, deleted_at TIMESTAMPTZ
  - 모든 Repository: findByIdAndIsDeletedFalse() 사용, findById() 직접 호출 금지
  - softDelete()는 BaseEntity protected, 각 Entity에서 public 래퍼 제공

공통 응답:
  - 성공: { success: true, code: "200", message: "...", data: {...} }
  - 실패: { success: false, code: "AUTH_401_INVALID_TOKEN", message: "...", data: null }
  - 에러코드 패턴: {DOMAIN}_{HTTP코드}_{설명}

에러코드:
  - 도메인별 enum: AuthErrorCode, UserErrorCode, ImageErrorCode, ExampleErrorCode
  - 공통: CommonErrorCode (EXTERNAL_API_ERROR 502, EXTERNAL_API_UNAVAILABLE 503)
  - 모두 BaseErrorCode 구현 → GeneralException → GlobalExceptionHandler 자동 처리

이미지 업로드:
  - 3단계 검증: MIME 타입 + 확장자 + 매직바이트(파일 시그니처)
  - contentType: 서버 측 EXT_TO_MIME 맵으로 고정 (클라이언트 헤더 무시)
  - S3 키: {directory}/{UUID}.{ext}

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
5. CI/CD 파이프라인
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
브랜치 전략:
  - develop: 기본 브랜치, PR 대상
  - main: 프로덕션 배포 전용
  - 이슈 생성 시 브랜치 자동 생성: [feat] 제목 → feat/#12-add-login (Groq API 번역)

CI (.github/workflows/ci.yml):
  - PR to develop → ./gradlew clean test
  - GitHub Actions services: postgres:16, redis:7

CD (.github/workflows/cd.yml):
  - push to main → Docker Hub push (projbase-api:latest)
  - GitHub Actions → AWS OIDC (SSH 키 없음) → EC2 SSM 배포
  - EC2에서 docker-compose-prod.yaml pull + up -d --build

GitHub Secrets (Repository secrets에 등록):
  DOCKER_HUB_USERNAME, DOCKER_HUB_TOKEN
  AWS_ACCOUNT_ID, AWS_REGION
  EC2_INSTANCE_ID (SSM 배포 대상)
  GROQ_API_KEY (이슈 브랜치 자동 생성용)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
6. AWS 인프라 아키텍처
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[클라이언트]
  → CloudFront (이미지 CDN, OAC)
  → S3 (이미지 원본, 퍼블릭 직접접근 차단)

[클라이언트]
  → EC2 (Ubuntu, Nginx → Docker Compose)
      ├── app (Spring Boot, :8080)
      ├── db  (PostgreSQL 16, :5432)
      └── redis (Redis 7, :6379)

EC2 IAM 역할 (instance profile):
  - SSM Session Manager (배포/접속용, SSH 포트 불필요)
  - S3 PutObject/DeleteObject (이미지 업로드)
  - CloudWatch Logs (선택)

GitHub Actions IAM 역할 (OIDC):
  - ssm:SendCommand + ssm:GetCommandInvocation (CD 배포)
  - 조건: token.actions.githubusercontent.com sub = repo:{org}/{repo}:ref:refs/heads/main

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
7. Terraform 인프라 세팅 (전체 HCL)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
아래 HCL을 infra/ 디렉터리에 작성하여 `terraform init && terraform apply` 로 전체 인프라를 생성한다.
{YOUR_*} 플레이스홀더를 실제 값으로 교체할 것.

--- infra/variables.tf ---
variable "aws_region"       { default = "ap-northeast-2" }
variable "project"          { default = "projbase" }
variable "github_org"       { description = "GitHub 조직 또는 사용자명" }
variable "github_repo"      { description = "GitHub 레포지토리명" }
variable "ec2_ami"          { description = "Ubuntu 22.04 LTS AMI ID (리전별 상이)" }
variable "ec2_instance_type"{ default = "t3.small" }
variable "ec2_key_name"     { description = "EC2 Key Pair 이름 (긴급 접속용, SSM 우선)" }
variable "s3_bucket_name"   { description = "이미지 버킷명 (전역 유니크)" }

--- infra/main.tf ---
terraform {
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 5.0" }
  }
}

provider "aws" { region = var.aws_region }

# ── VPC ──────────────────────────────────────────────────────────────────────
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = { Name = "${var.project}-vpc" }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "${var.project}-igw" }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.aws_region}a"
  map_public_ip_on_launch = true
  tags = { Name = "${var.project}-public-subnet" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
  tags = { Name = "${var.project}-public-rt" }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# ── Security Group ────────────────────────────────────────────────────────────
resource "aws_security_group" "ec2" {
  name   = "${var.project}-ec2-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  # SSH는 비상용으로만 포함. 평소에는 SSM 사용.
  ingress {
    description = "SSH (emergency only)"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "${var.project}-ec2-sg" }
}

# ── IAM: EC2 Instance Profile ─────────────────────────────────────────────────
data "aws_iam_policy_document" "ec2_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals { type = "Service", identifiers = ["ec2.amazonaws.com"] }
  }
}

resource "aws_iam_role" "ec2" {
  name               = "${var.project}-ec2-role"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume.json
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy" "s3_rw" {
  name = "${var.project}-s3-rw"
  role = aws_iam_role.ec2.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["s3:PutObject", "s3:DeleteObject", "s3:GetObject"]
      Resource = "${aws_s3_bucket.images.arn}/*"
    }]
  })
}

resource "aws_iam_instance_profile" "ec2" {
  name = "${var.project}-ec2-profile"
  role = aws_iam_role.ec2.name
}

# ── EC2 ───────────────────────────────────────────────────────────────────────
resource "aws_instance" "app" {
  ami                    = var.ec2_ami
  instance_type          = var.ec2_instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.ec2.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2.name
  key_name               = var.ec2_key_name

  user_data = <<-EOF
    #!/bin/bash
    set -e
    apt-get update -y
    apt-get install -y ca-certificates curl gnupg lsb-release

    # Docker 설치
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
      | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
      https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
      > /etc/apt/sources.list.d/docker.list
    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

    # ubuntu 사용자를 docker 그룹에 추가
    usermod -aG docker ubuntu

    # SSM Agent (Ubuntu에는 기본 설치, 확인용)
    systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service || true

    # Nginx 설치 (리버스 프록시)
    apt-get install -y nginx
    cat > /etc/nginx/sites-available/api <<'NGINX'
    server {
        listen 80;
        server_name _;
        location / {
            proxy_pass         http://localhost:8080;
            proxy_set_header   Host $host;
            proxy_set_header   X-Real-IP $remote_addr;
            proxy_set_header   X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header   X-Forwarded-Proto $scheme;
        }
    }
    NGINX
    ln -sf /etc/nginx/sites-available/api /etc/nginx/sites-enabled/api
    rm -f /etc/nginx/sites-enabled/default
    systemctl enable nginx
    systemctl start nginx

    # 앱 디렉터리 생성
    mkdir -p /home/ubuntu/app
    chown ubuntu:ubuntu /home/ubuntu/app
  EOF

  tags = { Name = "${var.project}-app-server" }
}

resource "aws_eip" "app" {
  instance = aws_instance.app.id
  domain   = "vpc"
  tags     = { Name = "${var.project}-eip" }
}

# ── S3 Bucket (이미지) ────────────────────────────────────────────────────────
resource "aws_s3_bucket" "images" {
  bucket        = var.s3_bucket_name
  force_destroy = false
  tags          = { Name = "${var.project}-images" }
}

resource "aws_s3_bucket_public_access_block" "images" {
  bucket                  = aws_s3_bucket.images.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "images" {
  bucket = aws_s3_bucket.images.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_versioning" "images" {
  bucket = aws_s3_bucket.images.id
  versioning_configuration { status = "Enabled" }
}

# ── CloudFront OAC ────────────────────────────────────────────────────────────
resource "aws_cloudfront_origin_access_control" "images" {
  name                              = "${var.project}-oac"
  description                       = "OAC for ${var.s3_bucket_name}"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_distribution" "images" {
  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = ""
  comment             = "${var.project} image CDN"

  origin {
    domain_name              = aws_s3_bucket.images.bucket_regional_domain_name
    origin_id                = "s3-${var.s3_bucket_name}"
    origin_access_control_id = aws_cloudfront_origin_access_control.images.id
  }

  default_cache_behavior {
    target_origin_id       = "s3-${var.s3_bucket_name}"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["GET", "HEAD"]
    cached_methods         = ["GET", "HEAD"]
    compress               = true

    forwarded_values {
      query_string = false
      cookies { forward = "none" }
    }

    min_ttl     = 0
    default_ttl = 86400
    max_ttl     = 31536000
  }

  restrictions {
    geo_restriction { restriction_type = "none" }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = { Name = "${var.project}-cdn" }
}

# S3 버킷 정책: CloudFront OAC에서만 접근 허용
resource "aws_s3_bucket_policy" "images" {
  bucket = aws_s3_bucket.images.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "AllowCloudFrontOAC"
      Effect    = "Allow"
      Principal = { Service = "cloudfront.amazonaws.com" }
      Action    = "s3:GetObject"
      Resource  = "${aws_s3_bucket.images.arn}/*"
      Condition = {
        StringEquals = {
          "AWS:SourceArn" = aws_cloudfront_distribution.images.arn
        }
      }
    }]
  })
}

# ── GitHub Actions OIDC ───────────────────────────────────────────────────────
data "aws_iam_openid_connect_provider" "github" {
  # 이미 계정에 OIDC Provider가 등록된 경우 이 data source 사용
  # 없는 경우 아래 resource로 생성
  url = "https://token.actions.githubusercontent.com"
}

# 계정에 OIDC Provider가 없을 경우 이 resource 사용 (data source와 택일)
# resource "aws_iam_openid_connect_provider" "github" {
#   url             = "https://token.actions.githubusercontent.com"
#   client_id_list  = ["sts.amazonaws.com"]
#   thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1"]
# }

data "aws_iam_policy_document" "github_actions_assume" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]
    principals {
      type        = "Federated"
      identifiers = [data.aws_iam_openid_connect_provider.github.arn]
    }
    condition {
      test     = "StringLike"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.github_org}/${var.github_repo}:ref:refs/heads/main"]
    }
    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "github_cd" {
  name               = "${var.project}-github-cd-role"
  assume_role_policy = data.aws_iam_policy_document.github_actions_assume.json
}

resource "aws_iam_role_policy" "github_cd_ssm" {
  name = "${var.project}-github-cd-ssm"
  role = aws_iam_role.github_cd.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["ssm:SendCommand"]
        Resource = [
          "arn:aws:ec2:${var.aws_region}:*:instance/${aws_instance.app.id}",
          "arn:aws:ssm:${var.aws_region}::document/AWS-RunShellScript"
        ]
      },
      {
        Effect   = "Allow"
        Action   = ["ssm:GetCommandInvocation"]
        Resource = "arn:aws:ssm:${var.aws_region}:*:*"
      }
    ]
  })
}

--- infra/outputs.tf ---
output "ec2_public_ip"       { value = aws_eip.app.public_ip }
output "ec2_instance_id"     { value = aws_instance.app.id }
output "cloudfront_domain"   { value = aws_cloudfront_distribution.images.domain_name }
output "s3_bucket"           { value = aws_s3_bucket.images.bucket }
output "github_cd_role_arn"  { value = aws_iam_role.github_cd.arn }

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
8. terraform apply 후 EC2 초기 설정 (SSM Session Manager로 접속)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
# .env.prod 파일 생성 (EC2 내부)
aws ssm start-session --target {EC2_INSTANCE_ID} --region ap-northeast-2

# EC2 내에서 실행:
sudo -i -u ubuntu
mkdir -p /home/ubuntu/app
cat > /home/ubuntu/app/.env.prod <<'ENV'
SPRING_PROFILES_ACTIVE=prod

# JWT
JWT_SECRET={base64-256bit-secret}
JWT_ACCESS_EXPIRATION=1800000
JWT_REFRESH_EXPIRATION=604800000

# Database (docker-compose 내부 서비스명 사용)
DB_HOST=db
DB_PORT=5432
DB_NAME=projbase
DB_USER=projbase
DB_PASSWORD={strong-db-password}

# Redis (docker-compose 내부 서비스명, 비밀번호 없음)
REDIS_HOST=redis
REDIS_PORT=6379

# OAuth
KAKAO_CLIENT_ID={kakao-rest-api-key}
KAKAO_CLIENT_SECRET={kakao-client-secret}
KAKAO_REDIRECT_URI=https://www.api.{your-domain}/api/v1/auth/oauth2/kakao/callback
GOOGLE_CLIENT_ID={google-client-id}
GOOGLE_CLIENT_SECRET={google-client-secret}
GOOGLE_REDIRECT_URI=https://www.api.{your-domain}/api/v1/auth/oauth2/google/callback

# AWS S3 + CloudFront
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET={s3-bucket-name}
AWS_CLOUDFRONT_DOMAIN={cloudfront-domain}.cloudfront.net

# OpenAI
OPENAI_API_KEY={openai-api-key}
ENV

# docker-compose-prod.yaml 업로드 (로컬에서)
scp -i {key}.pem docker-compose-prod.yaml ubuntu@{EC2_IP}:/home/ubuntu/app/
# 또는 git clone / scp 없이 SSM Send Command로 처리

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
9. GitHub Secrets 등록 목록
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Repository → Settings → Secrets and variables → Actions → Repository secrets:

  DOCKER_HUB_USERNAME   Docker Hub 사용자명
  DOCKER_HUB_TOKEN      Docker Hub Access Token (read/write)
  AWS_ACCOUNT_ID        terraform output으로 확인한 AWS Account ID
  AWS_REGION            ap-northeast-2
  EC2_INSTANCE_ID       terraform output ec2_instance_id
  GROQ_API_KEY          이슈 브랜치 자동 생성용 Groq API 키

CD workflow는 AWS OIDC 인증이므로 AWS_ACCESS_KEY_ID / SECRET 불필요.
OIDC role ARN은 cd.yml 의 role-to-assume에 하드코딩 또는 secret으로 관리.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
10. 도메인 연결 시 추가 작업
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. Route 53 (또는 외부 DNS): A 레코드 → EC2 EIP
2. EC2에서 Certbot으로 Let's Encrypt 인증서 발급:
   sudo certbot --nginx -d {your-domain.com}
3. Nginx sites-available/api 에 SSL 블록 추가 (certbot이 자동 수정)
4. SameSite=None + Secure 쿠키는 HTTPS 환경에서만 정상 동작
5. OAuth 콜백 URL을 각 플랫폼(카카오, 구글)에 HTTPS로 등록

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
11. 현재 미구현 (추가 개발 필요)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
- PATCH /api/v1/users/me (닉네임 수정 — Service 구현 완료, Controller 없음)
- DELETE /api/v1/users/me (회원 탈퇴 — 소프트 딜리트 인프라 완성)
- 테스트 코드 (단위 / 통합)
- Rate Limiting (로그인/회원가입 엔드포인트 — Bucket4j 또는 Redis 기반)
- CloudFront 커스텀 도메인 + ACM 인증서 (Terraform resource 추가 필요)
- RDS 전환 (현재 EC2 내 Docker PostgreSQL)
================================================================================
```
