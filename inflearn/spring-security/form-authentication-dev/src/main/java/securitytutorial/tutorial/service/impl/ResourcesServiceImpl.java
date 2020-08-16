package securitytutorial.tutorial.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import securitytutorial.tutorial.domain.entity.Resources;
import securitytutorial.tutorial.repository.ResourcesRespository;
import securitytutorial.tutorial.service.ResourcesService;

import java.util.List;

@Slf4j
@Service
public class ResourcesServiceImpl implements ResourcesService {

  @Autowired
  private ResourcesRespository resourcesRespository;

  @Override
  @Transactional
  public Resources getResources(long id) {
    return resourcesRespository.findById(id).orElse(new Resources());
  }

  @Override
  @Transactional
  public List<Resources> getResources() {
    return resourcesRespository.findAll(Sort.by(Sort.Order.asc("orderNum")));
  }

  @Override
  @Transactional
  public void createResources(Resources resources) {
    resourcesRespository.save(resources);

  }

  @Override
  @Transactional
  public void deleteResources(long id) {
    resourcesRespository.deleteById(id);
  }

}
