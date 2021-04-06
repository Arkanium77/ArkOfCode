package team.isaz.ark.user.repository.rest;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import team.isaz.ark.user.constants.Status;

@FeignClient(name = "${feign.core.name}", url = "${feign.core.url}")
public interface CoreServiceClient {
    @RequestMapping(path = "/internal/snippets/login", method = RequestMethod.PUT)
    Status updateLogin(@RequestParam String login, @RequestParam String newLogin,
                       @RequestHeader("Authorization") String token);
}
