package securitytutorial.tutorial.security.voter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import securitytutorial.tutorial.security.service.SecurityResourceService;

import java.util.Collection;
import java.util.List;

public class IpAddressVoter implements AccessDecisionVoter<Object> {


  private SecurityResourceService securityResourceService;

  public IpAddressVoter(SecurityResourceService securityResourceService) {
    this.securityResourceService = securityResourceService;
  }

  @Override
  public boolean supports(ConfigAttribute attribute) {
    return true;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return true;
  }

  /**
   *
   * @param authentication 인증 객체 정보
   * @param object         요청 정보
   * @param attributes     자원 권한 정보
   * @return
   */
  @Override
  public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
    WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
    String remoteAddress = details.getRemoteAddress();
    System.out.println(remoteAddress);

    List<String> accessIpList = securityResourceService.getAccessIpList();

    int result = ACCESS_DENIED;

    boolean abstain = accessIpList.stream().anyMatch(remoteAddress::equals);
    if (abstain) return ACCESS_ABSTAIN;

    if (result == ACCESS_DENIED) {
      throw new AccessDeniedException("Invalid IpAddress");
    }

    return result;
  }

}
