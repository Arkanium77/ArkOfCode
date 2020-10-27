package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.user.service.UserService;

import java.util.Objects;

@RestController
@RequestMapping("/secured")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/change_login")
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<String> changeLogin(@RequestParam String newLogin,
                                              @RequestHeader HttpHeaders headers) {
        String token = Objects.requireNonNull(headers.get("Authorization")).get(0);
        return userService.changeLogin(token, newLogin) ?
                new ResponseEntity<>("Login has been successfully changed. All old tokens have been recalled. Re-authorize.", HttpStatus.OK) :
                new ResponseEntity<>("Login change failed.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
