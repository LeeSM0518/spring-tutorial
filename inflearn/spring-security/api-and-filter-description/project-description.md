# 프로젝트 설명

## 보안 정책 설정

1. 자원 및 권한 설정
   * 마이페이지
     * 자원 설정 - /mypage
     * 권한 매핑 - ROLE_USER
   * 메시지
     * 자원 설정 - /message
     * 권한 매핑 - ROLE_MANAGER
   * 환경설정
     * 자원 설정 - /config
     * 권한 매핑 - ROLE_ADMIN
   * 관리자
     * 자원 설정 - /admin/**
     * 권한 매핑 - ROLE_ADMIN
2. 사용자 등록 및 권한부여
3. 권한계층적용
   * ROLE_ADMIN > ROLE_MANAGER > ROLE_USER
4. 메소드 보안 설정
   * 메소드 보안 - 서비스 계층 메소드 접근 제어
   * 포인트컷 보안 - 포인트컷 표현식에 따른 메소드 접근 제어
5. IP 제한하기