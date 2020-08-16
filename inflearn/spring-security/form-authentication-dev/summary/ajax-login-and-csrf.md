# 로그인 Ajax 구현 & CSRF

## 헤더 설정

* **전송 방식이 Ajax 인지의 여부를 위한 헤더 설정**

  ```java
  xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
  ```

* **CSRF 헤더 설정**

  ```html
  <meta id="_csrf" name="_csrf" th:content="$(_csrf.token)"/>
  <meta id="_csrf_header" name="_csrf_header" th:content="$(_csrf.headerName)"/>
  <script>
    var csrfHeader = $('meta[name="_csrf_header"]').attr('content');
    var csrfToken = $('meta[name="_csrf"]').attr('content');
    xhr.setRequestHeader(csrfHeader, csrfToken);
  </script>
  ```