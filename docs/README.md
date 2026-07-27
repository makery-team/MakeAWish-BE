# 📚 MakeAWish 백엔드(BE) 기술 문서 및 장애 분석 리포트 색인 (Documentation Index)

이 디렉토리(`docs/`)는 MakeAWish 백엔드 프로젝트의 장애 분석(Post-mortem), 아키텍처 및 설정 가이드 문서를 관리합니다.  
누구나 문서 이름만 보고도 어떤 내용인지 한눈에 파악할 수 있도록 **표준 문서 네이밍 규칙**을 준수합니다.

---

## 📐 문서 네이밍 규칙 (`YYYY-MM-DD_[분류]_[직관적_주제].md`)

문서 파일명은 작성 날짜와 문서의 성격(분류), 그리고 구체적인 핵심 주제를 영문 스네이크 케이스로 명확히 작성합니다.

```
예시: 2026-07-27_postmortem_aws_rds_mysql_deployment_failure.md
       (날짜)       (분류: 장애분석)      (주제: AWS RDS MySQL 배포 장애)
```

| 분류 접두어 | 설명 | 예시 |
| :---: | :--- | :--- |
| `postmortem_` | 장애 발생 원인 및 해결 과정을 기록한 사후 리포트 | `postmortem_aws_rds_mysql_deployment_failure.md` |
| `guide_` | 프로젝트 개발 환경, 배포, 인프라 설정 가이드 | `guide_local_environment_setup.md` |
| `api_` | API 명세서 및 외부 서비스(AI, 결제 등) 연동 가이드 | `api_ai_portfolio_tag_recommend.md` |
| `arch_` | 시스템 구조 및 도메인 아키텍처 설계 문서 | `arch_domain_model_and_erd.md` |

---

## 📋 문서 목록 (Index)

### 🚨 장애 분석 및 포스트모텀 (`postmortem_`)
| 작성일자 | 문서명 | 핵심 주제 및 해결 내용 |
| :---: | :--- | :--- |
| **2026-07-27** | [`2026-07-27_postmortem_aws_rds_mysql_deployment_failure.md`](./2026-07-27_postmortem_aws_rds_mysql_deployment_failure.md) | **AWS Elastic Beanstalk / RDS MySQL 실서버 배포 장애(502/500/403 에러) 최종 분석 리포트**<br>• 1차 원인: `DatabaseFixConfig.java` 중복 제약조건 실행과 예외 래핑(`BadSqlGrammarException`)으로 인한 서버 구동 크래시(502 에러)<br>• 2차 원인: `data.sql` 내 `TRUNCATE TABLE` 사용으로 RDS MySQL 외래키 제약조건(Error 1701) 충돌<br>• **결정적 원인:** `SecurityConfig.java` 내 `.requestMatchers(toH2Console())`가 비-H2 실서버 환경에서 예외를 일으켜 500/403 에러 발생 (주석 처리로 완벽 해결) |
