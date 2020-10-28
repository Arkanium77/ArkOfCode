package team.isaz.ark.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
        userService.changeLogin(token, newLogin);
        return new ResponseEntity<>("Login has been successfully changed. All old tokens have been recalled. Re-authorize.", HttpStatus.OK);
    }

    @PutMapping("/change_password")
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<String> changePassword(@RequestParam String newPassword,
                                                 @RequestHeader HttpHeaders headers) {
        String token = Objects.requireNonNull(headers.get("Authorization")).get(0);
        userService.changePassword(token, newPassword);
        return new ResponseEntity<>("Password has been successfully changed. All old tokens have been recalled. Re-authorize.", HttpStatus.OK);
    }

    @DeleteMapping("/delete_account")
    @Operation(
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<String> deleteAccount(@RequestHeader HttpHeaders headers) {
        String token = Objects.requireNonNull(headers.get("Authorization")).get(0);
        userService.deleteAccount(token);
        return new ResponseEntity<>("Your account has been successfully deleted! Thank you for being with us!", HttpStatus.OK);
    }

}
