# Junit Bank App

### Jpa LocalDateTime 자동으로 생성하는 법
- @EnableJpaAuditing (Main 클래스)
- @EntityListeners(AuditingEntityListener.class) (Entity 클래스)
```
    java
    @CreatedDate // insert
    @Column(nullable = false)
    private LocalDateTime createAt;
    
    @LastModifiedDate // Insert, Update
    @Column(nullable = false)
    private LocalDateTime updateAt;
```

### 기능
- 의존성 설정
- yml 설정
- 엔티티 설정
- 시큐리티 설정
- 시큐리티 테스트
- 레포지토리 생성
- 유효성 검사 테스트
- 회원가입
- 회원가입 테스트
- JWT 인증, 인가 테스트
- 계좌 등록
- 계좌 목록보기_유저별
- 계좌 삭제
- 계좌 입금
- 계좌 출금
- 계좌 이체
- 낮은 수 Long value() 테스트
- cors expose 테스트
- 컨트롤러 값 검증 테스트(서비스 단위 테스트만 하니까 전체 검증 필요)
- 이체 내역보기(동적 쿼리)
- 계좌 상세 보기