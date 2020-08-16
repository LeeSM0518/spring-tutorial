package securitytutorial.tutorial.security.metadatasource;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

public class UrlFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

  private LinkedHashMap<RequestMatcher, List<ConfigAttribute>> requestMap = new LinkedHashMap<>();

  @Override
  public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {

    HttpServletRequest request = ((FilterInvocation) object).getRequest();

    requestMap.put(new AntPathRequestMatcher("/mypage"), Arrays.asList(new SecurityConfig("ROLE_USER")));

    if (requestMap != null) {
      return requestMap.entrySet().stream()
          .filter(entry -> entry.getKey().matches(request))
          .findFirst()
          .map(Map.Entry::getValue)
          .orElseGet(ArrayList::new);

//      for (Map.Entry<RequestMatcher, List<ConfigAttribute>> entry :
//          requestMap.entrySet()) {
//        RequestMatcher matcher = entry.getKey();
//        if (matcher.matches(request)) {
//          return entry.getValue();
//        }
//      }
    }

    return null;
  }

  @Override
  public Collection<ConfigAttribute> getAllConfigAttributes() {
//    Set<ConfigAttribute> allAttributes = new HashSet<>();
//
//    for (Map.Entry<RequestMatcher, List<ConfigAttribute>> entry :
//        requestMap.entrySet()) {
//      allAttributes.addAll(entry.getValue());
//    }
//    return allAttributes;

    return requestMap.values().stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return FilterInvocation.class.isAssignableFrom(clazz);
  }

}
