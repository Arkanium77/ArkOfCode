package team.isaz.ark.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.isaz.ark.user.dto.Tokens;
import team.isaz.ark.user.dto.UserInfo;
import team.isaz.ark.user.entity.UserEntity;
import team.isaz.ark.user.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping("/public")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid UserInfo userInfo) {
        UserEntity userEntity = userService.registerUser(userInfo);
        return new ResponseEntity<>(userEntity.getLogin() + ", registration success! Now auth with your password.", HttpStatus.OK);
    }

    @PostMapping("/auth")
    public ResponseEntity<?> auth(@RequestBody UserInfo request) {
        Tokens tokens = userService.login(request);
        return tokens == null ?
                new ResponseEntity<>("Login failed! Try again!", HttpStatus.UNAUTHORIZED) :
                new ResponseEntity<>(tokens, HttpStatus.OK);
    }

    @GetMapping("/refresh")
    public ResponseEntity<Tokens> registerUser(@RequestParam String refreshToken) {
        Tokens tokens = userService.refreshToken(refreshToken);
        return new ResponseEntity<>(tokens, HttpStatus.OK);
    }
}
