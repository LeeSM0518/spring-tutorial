package securitytutorial.tutorial.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@Controller
public class MessageController {

  @GetMapping(value = "/messages")
  public String messages() throws Exception {
    return "user/messages";
  }

  @PostMapping("/api/messages")
  @ResponseBody
  public ResponseEntity<String> apiMessages() {
    return ResponseEntity.ok().body("ok");
  }

}


