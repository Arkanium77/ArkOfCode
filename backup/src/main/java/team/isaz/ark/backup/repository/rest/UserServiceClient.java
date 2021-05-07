package team.isaz.ark.backup.repository.rest;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import team.isaz.ark.backup.dto.TokenCheck;

@FeignClient(name = "${feign.user.name}")
public interface UserServiceClient {
    @RequestMapping(path = "/internal/bearer/check", method = RequestMethod.GET)
    TokenCheck checkToken(@RequestParam String bearerToken);
}
