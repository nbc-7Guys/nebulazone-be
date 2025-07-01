# Nebulazone-BE

Nebulazone-BE는 NBC-7Guys 팀의 클라우드 컴퓨팅 기반 프로젝트인 Nebulazone의 백엔드 저장소입니다. 이 프로젝트는 Docker와 Kubernetes를 활용한 컨테이너 기반의 분산 시스템 구축을 목표로 합니다.

---

## 🚀 프로젝트 소개
Nebulazone은 클라우드 환경에서 효율적이고 확장 가능한 서비스를 제공하기 위해 설계되었습니다. 이 백엔드 서비스는 프론트엔드 애플리케이션과 연동하여 사용자에게 다양한 기능을 제공합니다.

---

## 📖 ERD (Entity Relationship Diagram)

![ERD](https://github.com/nbc-7Guys/.github/blob/49b888a92c9f6628ccbb03a5bdca4a54ee9a99b3/images/erd.png)

---

### 주요 목표

- 컨테이너 기반 배포: Docker를 활용하여 서비스 컨테이너화.

- 오케스트레이션: Kubernetes를 이용하여 컨테이너의 배포, 확장, 관리 자동화.

- 안정적인 운영: 클라우드 환경에서의 고가용성 및 로드 밸런싱 구현.

---

## ⚙️ 기술 스택
이 프로젝트는 다음과 같은 기술 스택을 사용합니다:

### 백엔드

- Language: Java 21

- Framework: Spring Boot 3.x

- Database: MySQL, Redis

- ORM: Spring Data JPA

- Query: QueryDSL 6.11

- Security: Spring Security, JWT (jjwt 0.12.6)

### 인프라 & 배포

- Database: GCP VM (MySQL 8.0.42, Redis 8.0.2, Elasticsearch 8.18.1)

- Containerization: Docker

- Orchestration: Kubernetes

- Cloud Platform: GCP (VM, GCS, GKE, Cloudflare)

- CI/CD: Github Actions, ArgoCD

### 개발 도구

- IDE: IntelliJ IDEA

- Build Tool: Gradle

- Version Control: Git, GitHub

---

## 🏛️ 프로젝트 패키지 구조
우리 `nebulazone` 프로젝트는 역할과 책임에 따라 코드를 분리하는 **계층형 아키텍처(Layered Architecture)**를 기반으로 설계되었습니다. 이는 비즈니스 로직과 외부 기술을 분리하여 유연하고 테스트하기 쉬우며, 유지보수가 용이한 구조를 만드는 것을 목표로 합니다.

각 패키지의 주요 역할은 다음과 같습니다.

### 🌳 최상위 패키지 구조
- `└─nebulazone`

    - `├─application`: 응용 계층 (Application Layer) - 사용자의 요청을 받아 비즈니스 로직을 오케스트레이션하고, 결과를 반환합니다.

    - `├─domain`: 도메인 계층 (Domain Layer) - 프로젝트의 핵심 비즈니스 규칙과 데이터 모델을 포함합니다.

    - `├─infra`: 인프라 계층 (Infrastructure Layer) - 외부 기술(DB, Redis, Oauth 등)과의 연동을 담당합니다.

    - `├─common`: 공통 모듈 (Common Module) - 여러 계층에서 공통으로 사용되는 유틸리티, AOP, 예외 처리 등을 포함합니다.

    - `├─config`: 전역 설정 (Global Configuration) - 애플리케이션 전반에 걸친 설정 정보를 관리합니다.

### 📦 세부 패키지 설명
🔵 `domain` - **핵심 비즈니스 영역**

도메인 계층은 우리 서비스의 심장과 같습니다. 외부 기술에 의존하지 않는 순수한 비즈니스 로직과 데이터 모델(Entity)을 정의합니다.

- `├─user`, `├─auction`, `├─product`, 등: 각 비즈니스 도메인별로 패키지가 분리되어 있습니다.

    - `├─entity`: JPA `@Entity`로 정의된 핵심 데이터 모델입니다.

    - `├─repository`: 데이터 영속성을 위한 인터페이스(e.g., `UserRepository`)를 정의합니다.

    - `├─service`: 도메인 서비스. 특정 엔티티에 종속된 순수한 비즈니스 로직을 처리합니다.

    - `├─exception`: 해당 도메인에서 발생할 수 있는 비즈니스 예외(e.g., `UserNotFoundException`)를 정의합니다.

    - `├─vo` (Value Object): 값 객체. 특정 의미를 가지는 불변 객체(e.g., `Money`, `Address`)를 정의합니다.

### 🟢 application - 비즈니스 로직의 조율
응용 계층은 사용자의 요청을 직접 처리하는 진입점입니다. `domain` 계층의 서비스와 리포지토리를 조합하여 실제 사용 사례(Use Case)를 완성하고, 트랜잭션을 관리합니다.

- `├─user`, `├─auction`, `├─product`, 등: 각 기능별로 패키지가 분리되어 있습니다.

    - `├─controller`: HTTP 요청을 받아들이는 API 엔드포인트입니다.

    - `├─service`: 응용 서비스. 사용자의 요청 하나를 처리하기 위해 여러 도메인 서비스나 리포지토리를 조율(Orchestration)합니다. `@Transactional`이 주로 이 계층에 적용됩니다.

    - `├─dto` (Data Transfer Object): 계층 간 데이터 전송을 위한 객체입니다.

      - `├─request`: Controller가 클라이언트로부터 받는 요청 DTO입니다.

      - `├─response`: Controller가 클라이언트에게 반환하는 응답 DTO입니다.

### 🟡 infra - 외부 기술 연동
인프라 계층은 데이터베이스, 메시징 큐, 외부 API, 클라우드 서비스 등 외부 기술과의 연동을 책임집니다. `domain` 계층에 정의된 인터페이스를 구현하는 역할도 수행합니다.

- `├─redis`: Redis 연동(캐시, 분산 락, Pub/Sub) 관련 코드를 포함합니다.

- `├─gcs`: Google Cloud Storage 파일 업로드/다운로드 관련 코드를 포함합니다.

- `├─oauth`: 소셜 로그인(Naver, Kakao 등) 처리 로직을 포함합니다.

- `├─security`: Spring Security 기반의 인증/인가 및 JWT 필터 관련 설정을 포함합니다.

- `├─websocket`: 실시간 통신을 위한 웹소켓 관련 설정을 포함합니다.

- `├─payment`: 결제 연동 모듈을 포함합니다.

### ⚪ `common` & `config` - 공통 기능 및 설정
- `common`: 특정 계층이나 도메인에 속하지 않는 공통 기능들을 모아놓은 패키지입니다.

    - `├─aop`: 로깅, 성능 측정 등 횡단 관심사를 처리하는 Aspect 코드입니다.

    - `├─exception`: 전역 예외 처리를 위한 `@RestControllerAdvice`입니다.

    - `├─response`: API 응답 형식을 표준화하기 위한 래퍼 클래스입니다.

- `config`: QueryDSL, Swagger, 각종 Bean 등록 등 애플리케이션의 전역 설정 클래스들을 포함합니다.

### ✨ 이 구조의 장점
- **관심사의 분리 (Separation of Concerns)**: 비즈니스 로직과 기술 인프라가 분리되어 코드의 복잡성이 줄어듭니다.

- **유연성 및 확장성**: infra의 구현 기술을 변경해도(e.g., GCS → S3) domain 계층에 영향이 거의 없습니다.

- **테스트 용이성**: 핵심 로직인 domain 계층은 외부 기술 의존성이 없어 단위 테스트가 매우 용이합니다.

````
└─nebulazone
    ├─application
    │  ├─auction
    │  │  ├─consts
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  ├─scheduler
    │  │  └─service
    │  ├─auth
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  ├─metrics
    │  │  └─service
    │  ├─ban
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  └─service
    │  ├─bid
    │  │  ├─consts
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  ├─metrics
    │  │  └─service
    │  ├─catalog
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  └─service
    │  ├─chat
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  └─service
    │  ├─comment
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  └─service
    │  ├─notification
    │  │  ├─controller
    │  │  ├─dto
    │  │  └─service
    │  ├─pointhistory
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  └─service
    │  ├─post
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  └─service
    │  ├─product
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  ├─listener
    │  │  └─service
    │  ├─review
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  └─service
    │  ├─transaction
    │  │  ├─controller
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  └─service
    │  └─user
    │      ├─controller
    │      ├─dto
    │      │  ├─request
    │      │  └─response
    │      └─service
    ├─common
    │  ├─aop
    │  ├─exception
    │  ├─response
    │  └─util
    ├─config
    ├─domain
    │  ├─auction
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─exception
    │  │  ├─repository
    │  │  └─service
    │  ├─ban
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─exception
    │  │  ├─repository
    │  │  ├─scheduler
    │  │  └─service
    │  ├─bid
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─exception
    │  │  ├─repository
    │  │  └─service
    │  ├─catalog
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─exception
    │  │  ├─repository
    │  │  ├─service
    │  │  └─vo
    │  ├─chat
    │  │  ├─dto
    │  │  │  ├─request
    │  │  │  └─response
    │  │  ├─entity
    │  │  ├─exception
    │  │  ├─repository
    │  │  └─service
    │  ├─comment
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─exception
    │  │  ├─repository
    │  │  └─service
    │  ├─common
    │  │  ├─audit
    │  │  ├─constants
    │  │  └─validator
    │  │      └─image
    │  ├─notification
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─exception
    │  │  ├─repository
    │  │  └─service
    │  ├─pointhistory
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─exception
    │  │  ├─repository
    │  │  └─service
    │  ├─post
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─event
    │  │  ├─exception
    │  │  ├─listener
    │  │  ├─repository
    │  │  ├─service
    │  │  └─vo
    │  ├─product
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─event
    │  │  ├─exception
    │  │  ├─repository
    │  │  ├─service
    │  │  └─vo
    │  ├─review
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─exception
    │  │  ├─repository
    │  │  └─service
    │  ├─transaction
    │  │  ├─dto
    │  │  ├─entity
    │  │  ├─exception
    │  │  ├─repository
    │  │  └─service
    │  └─user
    │      ├─dto
    │      ├─entity
    │      ├─exception
    │      ├─repository
    │      └─service
    └─infra
        ├─config
        ├─gcs
        │  ├─client
        │  ├─config
        │  └─exception
        ├─oauth
        │  ├─dto
        │  ├─handler
        │  └─service
        ├─payment
        ├─redis
        │  ├─config
        │  ├─dto
        │  ├─lock
        │  ├─publisher
        │  ├─service
        │  ├─subscriber
        │  └─vo
        ├─security
        │  ├─config
        │  ├─constant
        │  ├─dto
        │  ├─exception
        │  └─filter
        │      └─exception
        └─websocket
            ├─config
            ├─dto
            ├─eventListener
            └─interceptor
````
