# MakeAWish CI/CD 구축 가이드 (GitHub Actions + AWS Elastic Beanstalk)

이 문서는 MakeAWish 프로젝트 백엔드 서버의 자동 배포(CI/CD) 과정과 개념을 정리한 가이드입니다.

## 1. CI/CD란 무엇인가요?

**CI/CD**는 개발자가 짠 코드를 자동으로 빌드, 테스트하고 실제 서비스 서버에 배포하는 전체 과정을 자동화하는 것을 뜻합니다.
우리가 방금 세팅한 것이 완벽한 형태의 CI/CD 파이프라인입니다.

*   **CI (Continuous Integration - 지속적 통합)**: 코드를 `main` 브랜치에 올리면(Merge), 깃허브가 자동으로 그 코드를 다운로드해서 `빌드(Build)`하고 에러가 없는지 검증합니다. (우리가 방금 수정한 Java 버전이나 Gradle 세팅이 여기서 사용됩니다.)
*   **CD (Continuous Deployment - 지속적 배포)**: CI 과정(빌드)이 성공적으로 끝나면, 완성된 결과물(ex: `.jar` 파일)을 실제 운영 중인 AWS 서버에 자동으로 밀어 넣고 켜줍니다.

> [!TIP]
> 이제 개발팀은 서버에 수동으로 접속하거나 직접 빌드할 필요 없이, **오직 깃허브에 코드를 병합(Merge)하는 작업 하나만으로 서버 배포를 완료**할 수 있습니다.

## 2. 우리가 구축한 파이프라인 구조

우리의 파이프라인은 크게 두 가지 플랫폼을 연결하여 구성되었습니다.

1.  **GitHub Actions (작업반장)**: `.github/workflows/deploy.yml`에 작성된 스크립트를 보고, 코드가 올라올 때마다 AWS 서버로 배포 작업을 지시하는 로봇입니다.
2.  **AWS Elastic Beanstalk (실서버)**: 실제 MakeAWish 백엔드 앱이 24시간 돌아가고 있는 라이브 서버입니다.

## 3. 세팅된 구성 요소 상세 내역

### 3.1. AWS IAM 계정 발급 및 등록
GitHub Actions 로봇이 AWS에 접속하려면 전용 열쇠가 필요합니다.
우리는 AWS 웹 콘솔에서 OTP 없이 프로그램 통신 전용으로 사용되는 **Access Key**를 발급받았습니다.
발급받은 키는 GitHub의 **Settings > Secrets and variables > Actions**에 안전하게 암호화하여 저장했습니다.
*   `AWS_ACCESS_KEY_ID`
*   `AWS_SECRET_ACCESS_KEY`

### 3.2. 배포 자동화 스크립트 (`deploy.yml`)
백엔드 리포지토리의 `.github/workflows/deploy.yml` 파일이 핵심 명령서입니다. 주요 단계는 다음과 같습니다:

1.  **트리거**: `main` 브랜치에 코드가 push(merge)될 때 작동을 시작합니다.
2.  **환경 세팅 (Set up JDK 21)**: 백엔드가 Java 21을 사용하고 있으므로, 배포 서버 환경도 Java 21로 맞춥니다. *(이전 빌드 오류가 났던 원인이 바로 이 부분이 17로 되어 있었기 때문이며 현재 21로 수정 완료되었습니다.)*
3.  **빌드 (Build with Gradle)**: `./gradlew build -x test` 명령어로 `.jar` 파일을 생성합니다.
4.  **AWS 배포 (Deploy to Elastic Beanstalk)**: 깃허브 비밀금고(Secrets)에 넣어둔 열쇠를 꺼내 AWS에 접속한 뒤, 방금 만든 `.jar` 파일을 `make-a-wish-env` 서버 환경으로 넘겨 배포를 마칩니다.

## 4. 트러블슈팅 이력
초기 구축 시 아래와 같은 오류들을 해결하여 자동화 파이프라인을 안정시켰습니다.

> [!WARNING]
> **1. 로컬 환경 종속성 문제 (gradle.properties)**
> 기존 프로젝트에 Windows 안드로이드 스튜디오용 로컬 Java 경로(`C:/Program Files/...`)가 하드코딩 되어있어 GitHub 리눅스 환경에서 빌드가 터지는 문제가 발생했습니다. 👉 **해당 설정을 제거하여 해결했습니다.**

> [!WARNING]
> **2. Java 버전 불일치 문제 (invalid source release: 21)**
> 백엔드 소스코드는 Java 21 문법을 쓰는데, GitHub Actions 빌드 환경이 Java 17로 세팅되어 있어 컴파일 에러가 발생했습니다. 👉 **deploy.yml 스크립트 내 버전을 `java-version: '21'`로 변경하여 해결했습니다.**

## 5. 결론 및 효과
이제 백엔드 개발자분은 코드 수정 후 GitHub에 PR을 올리고 Merge만 하면 됩니다. 인프라 배포에 신경 쓸 시간에 비즈니스 로직 개발에만 100% 집중할 수 있게 되었습니다!
