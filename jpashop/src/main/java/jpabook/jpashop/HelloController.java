package jpabook.jpashop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class HelloController {
    @RequestMapping("hello")
    public String hello(){
        log.info("home controller");
        return "home";

    }
}
