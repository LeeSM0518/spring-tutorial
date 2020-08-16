package securitytutorial.tutorial.controller.admin;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import securitytutorial.tutorial.domain.dto.ResourcesDto;
import securitytutorial.tutorial.domain.entity.Resources;
import securitytutorial.tutorial.domain.entity.Role;
import securitytutorial.tutorial.repository.RoleRepository;
import securitytutorial.tutorial.security.metadatasource.UrlFilterInvocationSecurityMetadataSource;
//import securitytutorial.tutorial.service.MethodSecurityService;
import securitytutorial.tutorial.service.ResourcesService;
import securitytutorial.tutorial.service.RoleService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class ResourceController {

  @Autowired
  private ResourcesService resourcesService;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RoleService roleService;

//  @Autowired
//  private MethodSecurityService methodSecurityService;

  @Autowired
  private UrlFilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource;

  @GetMapping(value = "/admin/resources")
  public String getResources(Model model) throws Exception {

    List<Resources> resources = resourcesService.getResources();
    model.addAttribute("resources", resources);

    return "admin/resource/list";
  }

  @PostMapping(value = "/admin/resources")
  public String createResources(ResourcesDto resourcesDto) throws Exception {

    ModelMapper modelMapper = new ModelMapper();
    Role role = roleRepository.findByRoleName(resourcesDto.getRoleName());
    Set<Role> roles = new HashSet<>();
    roles.add(role);
    Resources resources = modelMapper.map(resourcesDto, Resources.class);
    resources.setRoleSet(roles);

    resourcesService.createResources(resources);

    if ("url".equals(resourcesDto.getResourceType())) {
      filterInvocationSecurityMetadataSource.reload();
    } else {
//      methodSecurityService.addMethodSecured(resourcesDto.getResourceName(), resourcesDto.getRoleName());
    }

    return "redirect:/admin/resources";
  }

  @GetMapping(value = "/admin/resources/register")
  public String viewRoles(Model model) throws Exception {

    List<Role> roleList = roleService.getRoles();
    model.addAttribute("roleList", roleList);

    ResourcesDto resources = new ResourcesDto();
    Set<Role> roleSet = new HashSet<>();
    roleSet.add(new Role());
    resources.setRoleSet(roleSet);
    model.addAttribute("resources", resources);

    return "admin/resource/detail";
  }

  @GetMapping(value = "/admin/resources/{id}")
  public String getResources(@PathVariable String id, Model model) throws Exception {

    List<Role> roleList = roleService.getRoles();
    model.addAttribute("roleList", roleList);
    Resources resources = resourcesService.getResources(Long.valueOf(id));

    ModelMapper modelMapper = new ModelMapper();
    ResourcesDto resourcesDto = modelMapper.map(resources, ResourcesDto.class);
    model.addAttribute("resources", resourcesDto);

    return "admin/resource/detail";
  }

  @GetMapping(value = "/admin/resources/delete/{id}")
  public String removeResources(@PathVariable String id, Model model) throws Exception {

    Resources resources = resourcesService.getResources(Long.valueOf(id));
    resourcesService.deleteResources(Long.valueOf(id));

    if ("url".equals(resources.getResourceType())) {
      filterInvocationSecurityMetadataSource.reload();
    } else {
//      methodSecurityService.removeMethodSecured(resources.getResourceName());
    }

    return "redirect:/admin/resources";
  }


}
