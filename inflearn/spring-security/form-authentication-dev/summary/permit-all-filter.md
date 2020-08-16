# PermitAllFilter 구현

![image](https://user-images.githubusercontent.com/43431081/90334625-fe0fd780-e009-11ea-9b84-2cd062a2934f.png)

* `PermitAllFilter` 를 구현해서 내부 동작 원리에서 응용 동작 구현으로 변환

<br>

## 예시 코드

* `http.addFilterBefore(permitAllFilter(), FilterSecurityInterceptor.class)`

  ```java
  @Bean
  public PermitAllFiter permitAllFilter() {
    String[] permitAllPattern =
      ["/index", "/home", "/login", "/errorpage/**"];
    PermitAllFilter permitAllFilter = new PermitAllFilter(permitAllPattern);
    permitAllFilter.setAccessDecisionManager(accessDecisionManager);
    permitAllFilter.setSecurityMetadataSource(
      filterInvocationSecurityMetadataSource);
    permitAllFilter.setRejectPublicInvocations(false);
    return permitAllFilter;
  }
  ```

<br>

## 실제 코드

* **PermitAllFilter**

  ```java
  public class PermitAllFilter extends FilterSecurityInterceptor {
  
    private static final String FILTER_APPLIED = "__spring_security_filterSecurityInterceptor_filterApplied";
    private boolean observeOncePerRequest = true;
  
    private List<RequestMatcher> permitAllRequestMatchers = new ArrayList<>();
  
    public PermitAllFilter(String ... permitAllResources) {
      permitAllRequestMatchers = Arrays.stream(permitAllResources)
          .map(AntPathRequestMatcher::new)
          .collect(Collectors.toList());
    }
  
    @Override
    protected InterceptorStatusToken beforeInvocation(Object object) {
  
      HttpServletRequest request = ((FilterInvocation) object).getRequest();
      boolean permitAll = permitAllRequestMatchers.stream()
          .anyMatch(requestMatcher -> requestMatcher.matches(request));
  
      if (permitAll) {
        return null;
      }
  
      return super.beforeInvocation(object);
    }
  
    @Override
    public void invoke(FilterInvocation fi) throws IOException, ServletException {
      if ((fi.getRequest() != null)
          && (fi.getRequest().getAttribute(FILTER_APPLIED) != null)
          && observeOncePerRequest) {
        fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
      } else {
        if (fi.getRequest() != null && observeOncePerRequest) {
          fi.getRequest().setAttribute(FILTER_APPLIED, Boolean.TRUE);
        }
  
        InterceptorStatusToken token = beforeInvocation(fi);
  
        try {
          fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
        } finally {
          super.finallyInvocation(token);
        }
  
        super.afterInvocation(token, null);
      }
    }
  }
  ```

* **SecurityConfig**

  ```java
  @Configuration
  @EnableWebSecurity
  @Slf4j
  @Order(1)
  public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
    ...
  
    private String[] permitAllResources = {"/", "/login", "/user/login/**", "/login*", "/denied*"};
  
    ...
  
    @Bean
    public PermitAllFilter customFilterSecurityInterceptor() throws Exception {
      PermitAllFilter permitAllFilter = new PermitAllFilter(permitAllResources);
      permitAllFilter.setSecurityMetadataSource(
        urlFilterInvocationSecurityMetadataSource());
      permitAllFilter.setAccessDecisionManager(affirmativeBased());
      permitAllFilter.setAuthenticationManager(authenticationManagerBean());
      return permitAllFilter;
    }
  
    ...
  
  }
  ```

  