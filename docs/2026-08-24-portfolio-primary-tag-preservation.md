# 2026-08-24 포트폴리오 대표 태그(Primary Tag) 순서 영구 보존 가이드

## 1. 개요
- 사장님이 설정한 첫 번째 대표 태그(대표 뱃지)가 JPA 다대다(ManyToMany) 컬렉션 조회 시 DB tag_id 순서로 인해 섞이던 문제 해결
- Portfolio 엔티티에 primaryTag 필드를 추가하고, 응답 DTO(PortfolioFeedResponse, PortfolioResponse)에서 primaryTag를 항상 tags[0](첫 번째 인덱스)에 배치하여 소비자 앱 피드 뱃지 일치 보장

## 2. 변경 파일
1. src/main/java/org/makery/domain/Portfolio.java: primaryTag 필드 및 updateTagsAndPrimaryTag 추가
2. src/main/java/org/makery/service/PortfolioService.java: 등록/수정 시 request.getTags().get(0)을 primaryTag로 저장
3. src/main/java/org/makery/dto/PortfolioFeedResponse.java: primaryTag를 리스트의 0번째로 재정렬
4. src/main/java/org/makery/dto/PortfolioResponse.java: primaryTag를 리스트의 0번째로 재정렬
