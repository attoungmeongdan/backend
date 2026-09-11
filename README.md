<div align="center">

# ATMD Back-end Repository 🏋️
---
"카메라 하나로 내 운동을 분석하고, **AI 맞춤 운동처방**을 받는 서비스"
</br>
**ATMD**의 백엔드 저장소입니다.
</br>

![Intro](image/atmd_landing.png)

<br/>

</div>

---

## ✨ 핵심 기능

### 🔐 인증 및 회원 관리
- **OAuth 기반 소셜 로그인/회원가입 구현**
    - Google, Kakao 간편 로그인 지원
- **JWT 기반 인증 구조**
    - Access Token / Refresh Token 이중 토큰 방식 적용
    - Refresh Token Rotation으로 자동 로그인 및 보안성 강화

### 🤸 실시간 운동 자세 분석
- **WebSocket 기반 실시간 포즈 분석**
    - 브라우저의 MediaPipe WASM이 관절 좌표(Landmark)를 추출해 서버로 전송
    - 서버에서 각도 계산 후 자세 피드백 즉시 반환
- **지원 운동 종류**
    - 푸시업(Push-up), 윗몸일으키기(Sit-up), 플랭크(Plank), 의자앉았다일어서기(Chair Stand)

### 📊 체력 측정 및 평가
- **국민체력100 기준 체력 측정 모드**
    - 연령·성별 그룹별 백분위 산출
    - 동일 코호트 평균 대비 성과 비교 제공
- **측정 결과 분석**
    - 회차별 기록 히스토리 및 변화 추세 시각화
    - 퍼센타일 구간 및 피어 비교 데이터 제공

### 🤖 AI 운동처방 (RAG)
- **pgvector + OpenAI Embedding 기반 유사도 검색**
    - 운동처방 데이터를 벡터 임베딩해 사용자 측정 결과와 유사한 처방 검색
- **GPT-4o-mini 기반 인사이트 생성**
    - 측정 결과를 바탕으로 맞춤 운동처방 텍스트 생성

### 🗺️ 주변 운동시설 조회
- **위치 기반 반경 5km 이내 시설 목록 제공**
    - Haversine 공식 기반 거리 계산 및 거리순 정렬 반환
    - 조회 결과 Redis 24시간 캐시로 응답 속도 최적화

### 📅 운동 캘린더 & 기록 관리
- **날짜별 운동 기록 조회**
    - 최근 운동 현황 요약 제공
- **이미지 관리**
    - AWS S3 업로드 + CloudFront CDN 배포

---

## ⚙ 기술 스택
|    파트    |                                                      기술                                                       |
|:--------:|:-------------------------------------------------------------------------------------------------------------:|
| BackEnd  | Java 17, Spring Boot 3.5.3, Spring Security, JPA, QueryDSL, WebSocket, OpenFeign, MapStruct, Flyway, AOP |
| DataBase |                                          PostgreSQL 16 + pgvector, Redis 7                                          |
|    AI    |                                  OpenAI API (gpt-4o-mini, text-embedding-3-small), RAG                                   |
|  Infra   |                                    Docker, AWS EC2, AWS S3, CloudFront, SSM, CloudWatch                                     |
|  CI/CD   |                                              GitHub Actions, Docker Hub                                               |
|   etc.   |                                              Swagger, Prometheus, Notion, Discord                                              |

---

## 💁‍♂️ 프로젝트 팀원
<div align="center">
<table>
<tr>
<td align="center" style="width: 150px; padding: 10px;">
<img src="https://github.com/hyeonkangkimm.png" width="200"/><br/>
<b>김현강</b><br/>
<sub>역할</sub>
</td>
<td align="center" style="width: 150px; padding: 10px;">
<img src="https://github.com/catomat0.png" width="200"/><br/>
<b>김동국</b><br/>
<sub>역할</sub>
</td>
<td align="center" style="width: 150px; padding: 10px;">
<img src="https://github.com/rkdehdrbs7885-oss.png" width="200"/><br/>
<b>강동균</b><br/>
<sub>역할</sub>
</td>
</tr>
</table>
</div>

---

## 🧺 ETC.

### ERD 구조
<div align="center">
<img src="./image/atmd_backend.png" width="700"/><br/>
</div>

### 아키텍처
<div align="center">
<img src="./image/atmd_infra.png" width="600"/><br/>
</div>

### 설계 원칙
도메인형 패키지 구조를 기반으로, 비즈니스 로직과 핵심 데이터 패키지를 분리한 구조입니다.</br>
[컨벤션 정리](https://wiry-tuck-17c.notion.site/3d43538f6f54803aaa31d5348da33d1a?source=copy_link)
